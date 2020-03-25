/*
 * Copyright 2016 National Bank of Belgium
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
package internal.bytes;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Facade to prevent ByteBuffer pitfalls.
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bytes implements BytesReader, BytesWriter {

    @NonNull
    public static Bytes allocate(int capacity, @NonNull ByteOrder order) {
        return new Bytes(ByteBuffer.allocate(capacity).order(order));
    }

    @NonNull
    public static Bytes wrap(@NonNull byte[] bytes, @NonNull ByteOrder order) {
        return new Bytes(ByteBuffer.wrap(bytes).order(order));
    }

    @lombok.NonNull
    private final ByteBuffer internal;

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Bytes && equals((Bytes) obj));
    }

    private boolean equals(Bytes that) {
        return this.internal.equals(that.internal);
    }

    @Override
    public int hashCode() {
        return internal.hashCode();
    }

    @Override
    public int getLength() {
        return internal.capacity();
    }

    @Override
    public byte getByte(int index) {
        return internal.get(index);
    }

    @Override
    public short getInt16(int index) {
        return internal.getShort(index);
    }

    @Override
    public int getInt32(int index) {
        return internal.getInt(index);
    }

    @Override
    public long getInt64(int index) {
        return internal.getLong(index);
    }

    @Override
    public float getFloat32(int index) {
        return internal.getFloat(index);
    }

    @Override
    public double getFloat64(int index) {
        return internal.getDouble(index);
    }

    @Override
    public ByteOrder getOrder() {
        return internal.order();
    }

    @Override
    public BytesReader slice(int index, int length) {
        fixJava9(internal).position(index);
        fixJava9(internal).limit(index + length);
        ByteBuffer result = internal.slice().order(internal.order());
        fixJava9(internal).clear();
        return new Bytes(result);
    }

    @Override
    public String getString(int index, int length, Charset charset) {
        return length > 0 ? new String(getBytes(index, length), charset).trim() : "";
    }

    @Override
    public void copyTo(int index, byte[] dst, int dstPos, int length) {
        fixJava9(internal).position(index);
        internal.get(dst, dstPos, length);
        fixJava9(internal).position(0);
    }

    @Override
    public byte[] getBytes(int index, int length) {
        byte[] result = new byte[length];
        copyTo(index, result, 0, length);
        return result;
    }

    @Override
    public CharBuffer decode(int index, int length, CharsetDecoder decoder) throws CharacterCodingException {
        fixJava9(internal).position(index);
        fixJava9(internal).limit(index + length);
        CharBuffer result = decoder.decode(internal);
        fixJava9(internal).clear();
        return result;
    }

    @Override
    public BytesReader duplicate(ByteOrder order) {
        return new Bytes(internal.duplicate().order(order));
    }

    @Override
    public void fill(SeekableByteChannel sbc, long position) throws IOException {
        fixJava9(internal).clear();
        sbc.position(position);
        sbc.read(internal);
        fixJava9(internal).clear();
    }

    @Override
    public void putByte(int index, byte b) {
        internal.put(index, b);
    }

    // See https://jira.mongodb.org/browse/JAVA-2559
    private static Buffer fixJava9(ByteBuffer buffer) {
        return buffer;
    }

    // CONVENIENT ALIASES
    public static final int SHORT_OFFSET = Short.BYTES;
    public static final int INT_OFFSET = Integer.BYTES;
    public static final int LONG_OFFSET = Long.BYTES;
}
