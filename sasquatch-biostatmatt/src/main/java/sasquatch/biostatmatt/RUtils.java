/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package sasquatch.biostatmatt;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author Philippe Charles
 */
public final class RUtils {

    private RUtils() {
        // static class
    }

    //<editor-fold defaultstate="collapsed" desc="R structures">
    public interface RObject {
    }

    public interface RFrame extends RObject {

        RList<RVector<Object>> getData();

        Map<String, Object> getAttributes();
    }

    public interface RList<T> extends RObject, Iterable<T> {

        void set(int rIndex, T value);

        void set(String name, T value);

        T get(int rIndex);

        T get(String name);

        RList<T> filter(Iterable<Boolean> filter);
    }

    public interface RVector<T> extends RObject, Iterable<T> {

        void set(int rIndex, T value);

        T get(int rIndex);
    }

    public interface RCon {
    }

    public interface RData {

        RFrame frame(RList<RVector<Object>> data);
    }

    public interface RMake {

        <X> RList<X> unique(Iterable<X> source);
    }

    public enum DataType {

        NUMERIC, CHARACTER, RAW, INTEGER, DOUBLE
    }

    public interface RAs {

        RData data = new RDataImpl();

        RVector<Byte> raw(int... vector);

        double POSIXct(String x, String format);
    }

    public interface RIs {

        boolean character(Object file);
    }

    public interface RFunc<X, Y> {

        Y apply(X input);
    }
    //</editor-fold>

    public static final RAs as = new RAsImpl();
    public static final RMake make = new RMakeImpl();
    public static final RIs is = new RIsImpl();

    // Character vectors
    public static String[] c(String... input) {
        return input;
    }

    public static int[] c(int... input) {
        return input;
    }

    public static int[] c(int[]... list) {
        int totalLength = 0;
        for (int[] o : list) {
            totalLength += o.length;
        }
        int[] result = new int[totalLength];
        int pos = 0;
        for (int[] o : list) {
            System.arraycopy(o, 0, result, pos, o.length);
            pos += o.length;
        }
        return result;
    }

    public static RVector<Byte> c(RVector<Byte> a, RVector<Byte> b) {
        return ((RawRVector) a).concat(((RawRVector) b));
    }

    public static <T> RList<T> c(RList<T>... input) {
        RList<T> result = list();
        for (RList<T> list : input) {
            for (T o : list) {
                result.set(length(result) + 1, o);
            }
        }
        return result;
    }

    public static int length(RObject input) {
        return ((RObjectImpl) input).getLength();
    }

    public static int length(byte[] array) {
        return array.length;
    }

    public static boolean _in_(int val, int[] list) {
        for (int o : list) {
            if (o == val) {
                return true;
            }
        }
        return false;
    }

    public static boolean identical(RVector<Byte> l, RVector<Byte> r) {
        return objectEquals(l, r);
    }

    public static boolean identical(String l, String r) {
        return objectEquals(l, r);
    }

    public static RVector<Byte> _sub_(RVector<Byte> buf, int from, int to) {
        return ((RawRVector) buf).sub(from, to - from + 1);
    }

    public static Object readBin(RCon rawVector, DataType what, int n, int size) {
        RawRVector raw = ((FileRCon) rawVector).getRaw();
        Object result = readBin(raw, what, n, size);
        // update of position in bytebuffer to mimic stream behavior
        if (!raw.equals(RawRVector.EMPTY)) {
            raw.byteBuffer.position(raw.byteBuffer.position() + n * size);
        }
        return result;
    }

    /**
     * Read binary data from a connection.
     *
     * @param rawVector A connection object or a character string naming a file
     * or a raw vector.
     * @param what Either an object whose mode will give the mode of the vector
     * to be read, or a character vector of length one describing the mode: one
     * of "numeric", "double", "integer", "int", "logical", "complex",
     * "character", "raw".
     * @param n integer. The (maximal) number of records to be read. You can use
     * an over-estimate here, but not too large as storage is reserved for n
     * items.
     * @param size integer. The number of bytes per element in the byte stream.
     * The default, NA_integer_, uses the natural size. Size changing is not
     * supported for raw and complex vectors.
     * @return a vector of appropriate mode and length the number of items read
     * (which might be less than n).
     * @see http://stat.ethz.ch/R-manual/R-devel/library/base/html/readBin.html
     */
    public static Object readBin(RVector<Byte> rawVector, DataType what, int n, int size) {
        checkArgument(n > 0, "Invalid number of records to be read");
        checkArgument(size > 0, "Invalid number of bytes per element in the byte stream");
        RawRVector buf = (RawRVector) rawVector;
        // use of absolute index to prevent update of position in bytebuffer
        int index = buf.byteBuffer.position();
        switch (what) {
            case RAW:
                int length = Math.min(buf.getLength(), n * size);
                return length > 0 ? buf.sub(index + 1, length) : RawRVector.EMPTY;
            case NUMERIC:
            case DOUBLE:
                checkArgument(n == 1, "Can only read 1 record at a time");
                switch (size) {
                    case 8:
                        return buf.byteBuffer.getDouble(index);
                    case 4:
                        return buf.byteBuffer.getFloat(index);
                    default:
                        throw new IllegalArgumentException("Invalid size for type numeric");
                }
            case CHARACTER:
                return buf.getString(index + 1, n * size);
            case INTEGER:
                checkArgument(n == 1, "Can only read 1 record at a time");
                switch (size) {
                    case 8:
                        return buf.byteBuffer.getLong(index);
                    case 4:
                        return buf.byteBuffer.getInt(index);
                    case 2:
                        return buf.byteBuffer.getShort(index);
                    case 1:
                        return buf.byteBuffer.get(index);
                    default:
                        throw new IllegalArgumentException("Invalid size for type integer");
                }
            default:
                throw new UnsupportedOperationException("Datatype not supported: " + what.name());
        }
    }

    public static void stop(String cause) throws IOException {
        throw new IOException(cause);
    }

    /**
     * Concatenate vectors after converting to character.
     *
     * @param input one or more R objects, to be converted to character vectors.
     * @return A character vector of the concatenated values.
     */
    public static String paste(Object... input) {
        if (input.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(input[0]);
        for (int i = 1; i < input.length; i++) {
            result.append(" ").append(input[i]);
        }
        return result.toString();
    }

    public static <T> RList<T> list() {
        return new RListImpl<>();
    }

    public static <T> RList<T> list(long length) {
        throw new UnsupportedOperationException("TODO");
    }

    public static <X, Y> Iterable<Y> sapply(RList<X> source, RFunc<X, Y> func) {
        List<Y> result = new ArrayList<>();
        for (X value : source) {
            result.add(func.apply(value));
        }
        return result;
    }

    public static void warning(String str) {
        System.out.println(str);
    }

    /**
     * Replicate Elements of Vectors and Lists.
     *
     * @param x
     * @param times
     * @return
     * @see http://stat.ethz.ch/R-manual/R-devel/library/base/html/rep.html
     */
    public static int[] rep(int x, int times) {
        int[] result = new int[times];
        Arrays.fill(result, x);
        return result;
    }

    public static RVector vector(DataType type, int length) {
        return new CustomRVector(type, length);
    }

    /**
     * Perform replacement of all matches respectively.
     *
     * @param pattern
     * @param replacement
     * @param x
     * @return
     */
    public static Object gsub(String pattern, String replacement, Object x) {
        checkArgument("^ +| +$".equals(pattern), "Pattern '%s' not supported yet", pattern);
        checkArgument("".equals(replacement), "Replacement '%s' not supported yet", replacement);
        return x.toString().trim();
    }

    public static boolean inherits(Object file, String options) {
        checkArgument("connection".equals(options), "Option '%s' not supported yet", options);
        return file instanceof FileRCon;
    }

    public static boolean isOpen(Object file, String options) {
        checkArgument("read".equals(options), "Option '%s' not supported yet", options);
        return ((FileRCon) file).isLoaded();
    }

    public static void close(RCon o) throws IOException {
        ((FileRCon) o).close();
    }

    public static RCon file(String file, String options) throws IOException {
        return new FileRCon(Paths.get(file).toFile());
    }

    public static void attr(RFrame data, String name, Object value) {
        data.getAttributes().put(name, value);
    }

    public static int[] seq(int from, int to) {
        int[] result = new int[to - from + 1];
        for (int i = from; i <= to; i++) {
            result[i - from] = i;
        }
        return result;
    }

    /**
     * Read or set the declared encodings for a character vector.
     * https://stat.ethz.ch/R-manual/R-devel/library/base/html/Encoding.html
     *
     * @param input
     * @param encoding
     * @return
     */
    public static Object encoding(Object input, String encoding) {
        try {
            return encoding.isEmpty() ? input : new String(((String) input).getBytes(DEFAULT_CHARSET), Charset.forName(encoding));
        } catch (UnsupportedCharsetException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation specifics">
    private static boolean objectEquals(Object l, Object r) {
        return l == r || (l != null && l.equals(r));
    }

    private static int jIndex(int rIndex) {
        return rIndex - 1;
    }

    private static void checkArgument(boolean expression, String format, Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, format, args));
        }
    }

    private static void checkRBounds(int rIndex, int min, int max) {
        if (rIndex < 1 || rIndex < min || rIndex >= max) {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "index=%s min=%s max=%s", rIndex, min, max));
        }
    }

    static abstract class RObjectImpl implements RObject {

        abstract int getLength();
    }

    private static final Charset DEFAULT_CHARSET = Charset.forName("windows-1252");

    public static final class RawRVector extends RObjectImpl implements RVector<Byte> {

        public static final RawRVector EMPTY = new RawRVector(ByteBuffer.allocate(0));
        public final ByteBuffer byteBuffer;

        RawRVector(ByteBuffer byteBuffer) {
            this.byteBuffer = Objects.requireNonNull(byteBuffer);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof RawRVector && equals((RawRVector) obj));
        }

        private boolean equals(RawRVector other) {
            return byteBuffer.equals(other.byteBuffer);
        }

        @Override
        public int hashCode() {
            return byteBuffer.hashCode();
        }

        RawRVector concat(RawRVector that) {
            byte[] bytes = new byte[this.getLength() + that.getLength()];
            this.byteBuffer.duplicate().get(bytes, 0, this.getLength());
            that.byteBuffer.duplicate().get(bytes, this.getLength(), that.getLength());
            return new RawRVector(ByteBuffer.wrap(bytes).order(this.byteBuffer.order()));
        }

        RawRVector sub(int rIndex, int length) {
            checkRBounds(rIndex, 1, getLength() + 1);
            ByteBuffer tmp = byteBuffer.duplicate();
            tmp.position(jIndex(rIndex));
            tmp.limit(jIndex(rIndex) + length);
            return new RawRVector(tmp.slice().order(byteBuffer.order()));
        }

        String getString(int rIndex, int length) {
            checkRBounds(rIndex, 1, getLength() + 1);
            byte[] bytes = new byte[length];
            ByteBuffer tmp = byteBuffer.duplicate();
            tmp.position(jIndex(rIndex));
            tmp.get(bytes);
            return new String(bytes, DEFAULT_CHARSET);
        }

        @Override
        public void set(int rIndex, Byte value) {
            checkRBounds(rIndex, 1, getLength() + 1);
            byteBuffer.put(jIndex(rIndex), value);
        }

        @Override
        public Byte get(int rIndex) {
            checkRBounds(rIndex, 1, getLength() + 1);
            return byteBuffer.get(jIndex(rIndex));
        }

        @Override
        public int getLength() {
            return byteBuffer.capacity();
        }

        @Override
        public Iterator<Byte> iterator() {
            return new Iterator<Byte>() {
                int cursor = 0;

                @Override
                public boolean hasNext() {
                    return cursor < byteBuffer.capacity();
                }

                @Override
                public Byte next() {
                    return byteBuffer.get(cursor++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
    }

    private static final class RAsImpl implements RAs {

        static final long SAS_EPOCH = -315619200;

        @Override
        public RVector<Byte> raw(int... vector) {
            Objects.requireNonNull(vector);
            byte[] result = new byte[vector.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) vector[i];
            }
            return new RawRVector(ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN));
        }

        @Override
        public double POSIXct(String x, String format) {
            checkArgument("1960/01/01".equals(x), "Input not supported yet");
            checkArgument("%Y/%m/%d".equals(format), "Format not supported yet");
            return SAS_EPOCH;
        }
    }

    private static final class RIsImpl implements RIs {

        @Override
        public boolean character(Object file) {
            return file instanceof String;
        }
    }

    private static final class FileRCon implements RCon, Closeable {

        private final RandomAccessFile source;
        private final FileChannel channel;
        private final MappedByteBuffer byteBuffer;
        private final RawRVector raw;

        FileRCon(File file) throws IOException {
            source = new RandomAccessFile(file, "r");
            channel = source.getChannel();
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            byteBuffer.load();
            raw = new RawRVector(byteBuffer.order(ByteOrder.LITTLE_ENDIAN));
        }

        public RawRVector getRaw() {
            return raw;
        }

        public boolean isLoaded() {
//            return byteBuffer.isLoaded();
            // FIXME: doesn't work ?
            return true;
        }

        @Override
        public void close() throws IOException {
            byteBuffer.clear();
            channel.close();
            source.close();
        }
    }

    private static final class RListImpl<T> extends RObjectImpl implements RList<T> {

        private final List<T> data = new ArrayList<>();
        private final Map<String, Integer> index = new HashMap<>();

        @Override
        int getLength() {
            return data.size();
        }

        @Override
        public void set(int rIndex, T value) {
            checkRBounds(rIndex, 1, getLength() + 2);
            data.add(value);
        }

        @Override
        public void set(String name, T value) {
            data.add(value);
            index.put(name, data.size() - 1);
        }

        @Override
        public T get(int rIndex) {
            checkRBounds(rIndex, 1, getLength() + 1);
            return data.get(jIndex(rIndex));
        }

        @Override
        public T get(String name) {
            return data.get(index.get(name));
        }

        @Override
        public RList<T> filter(Iterable<Boolean> filter) {
            RListImpl<T> result = new RListImpl<>();
            Iterator<Boolean> iter1 = filter.iterator();
            Iterator<T> iter2 = this.iterator();
            while (iter1.hasNext() && iter2.hasNext()) {
                boolean filtered = iter1.next();
                T value = iter2.next();
                if (filtered) {
                    result.data.add(value);
                }
            }
            return result;
        }

        @Override
        public Iterator<T> iterator() {
            return data.iterator();
        }

        @Override
        public String toString() {
            return new StringBuilder().append("List {")
                    .append(" length=").append(getLength())
                    .append(" }").toString();
        }
    }

    private static final class RMakeImpl implements RMake {

        @Override
        public <X> RList<X> unique(Iterable<X> source) {
            RListImpl<X> result = new RListImpl<>();
            for (X o : source) {
                if (!result.data.contains(o)) {
                    result.data.add(o);
                }
            }
            return result;
        }
    }

    static final class CustomRVector extends RObjectImpl implements RVector {

        final ArrayList<Object> data;
        final Class<?> javaDataType;
        final int maxLength;

        CustomRVector(DataType dataType, int length) {
            this.data = new ArrayList<>();
            switch (dataType) {
                case CHARACTER:
                    this.javaDataType = String.class;
                    break;
                case NUMERIC:
                    this.javaDataType = Number.class;
                    break;
                default:
                    throw new UnsupportedOperationException("TODO");
            }
            this.maxLength = length;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof CustomRVector && equals((CustomRVector) obj));
        }

        private boolean equals(CustomRVector other) {
            return data.equals(other.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public void set(int rIndex, Object value) {
            Objects.requireNonNull(value);
            checkArgument(javaDataType.isInstance(value), "Invalid data type: expected=%s found=%s", javaDataType, value.getClass());
            checkRBounds(rIndex, 1, getLength() + 1);
            int diff = rIndex - data.size();
            if (diff > 0) {
                if (diff > 1) {
                    data.ensureCapacity(rIndex);
                    for (int i = 0; i < diff - 1; i++) {
                        data.add(null);
                    }
                }
                data.add(value);
            } else {
                data.set(jIndex(rIndex), value);
            }
        }

        @Override
        public Object get(int rIndex) {
            checkRBounds(rIndex, 1, getLength() + 1);
            int index = jIndex(rIndex);
            return index < data.size() ? data.get(index) : null;
        }

        @Override
        int getLength() {
            return maxLength;
        }

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                int cursor = 0;

                @Override
                public boolean hasNext() {
                    return cursor < maxLength;
                }

                @Override
                public Object next() {
                    return get(++cursor);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        @Override
        public String toString() {
            return new StringBuilder().append("Vector {")
                    .append(" length=").append(getLength())
                    .append(" dataType=").append(javaDataType)
                    .append(" }").toString();
        }
    }

    private static final class RDataImpl implements RData {

        @Override
        public RFrame frame(RList<RVector<Object>> data) {
            return new RFrameImpl(data);
        }
    }

    private static final class RFrameImpl extends RObjectImpl implements RFrame {

        private final RList<RVector<Object>> data;
        private final Map<String, Object> attributes;

        RFrameImpl(RList<RVector<Object>> data) {
            this.data = data;
            this.attributes = new LinkedHashMap<>();
        }

        @Override
        int getLength() {
            return ((RObjectImpl) data).getLength();
        }

        @Override
        public RList<RVector<Object>> getData() {
            return data;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }
    //</editor-fold>
}
