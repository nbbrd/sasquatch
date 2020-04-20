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
package sasquatch;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sasquatch.spi.SasReader;
import sasquatch.spi.SasReaderLoader;

/**
 * A thread-safe reader used to read SAS datasets (*.sas7bdat).
 *
 * @author Philippe Charles
 */
//@ThreadSafe
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Sasquatch {

    @NonNull
    public static Sasquatch ofServiceLoader() {
        return new Sasquatch(SasReaderLoader.load().stream().findFirst());
    }

    @NonNull
    public static Sasquatch of(@NonNull SasReader reader) {
        return new Sasquatch(Optional.of(reader));
    }

    @lombok.NonNull
    private final Optional<SasReader> reader;

    /**
     * Read a SAS dataset into a forward cursor.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The cursor is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public SasForwardCursor readForward(@NonNull Path file) throws IOException {
        Objects.requireNonNull(file);
        return getReader().readForward(file);
    }

    /**
     * Read a SAS dataset into a scrollable cursor.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The cursor is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public SasScrollableCursor readScrollable(@NonNull Path file) throws IOException {
        Objects.requireNonNull(file);
        return getReader().readScrollable(file);
    }

    /**
     * Read the metadata of a SAS dataset.
     * <p>
     * Note that this same metadata can also be obtained by using the read
     * method. <br>This is a shortcut when you don't need data (the resources
     * are automatically released).
     *
     * @param file the SAS dataset to read
     * @return a non-null metadata
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public SasMetaData readMetaData(@NonNull Path file) throws IOException {
        Objects.requireNonNull(file);
        return getReader().readMetaData(file);
    }

    /**
     * Read all rows of a SAS dataset as a {@code Stream}. Unlike {@link
     * #getAllRows(Path, Charset) getAllRows}, this method does not read all
     * lines into a {@code List}, but instead populates lazily as the stream is
     * consumed.
     *
     * @apiNote This method must be used within a try-with-resources statement
     * or similar control structure to ensure that the stream's open file is
     * closed promptly after the stream's operations have completed.
     *
     * @param <T>
     * @param file the SAS dataset to read
     * @param rowMapper
     * @return a non-null stream
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public <T> Stream<T> rows(@NonNull Path file, @NonNull SasRowMapper<T> rowMapper) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(rowMapper);
        SasForwardCursor cursor = readForward(file);
        return streamOf(cursor, rowMapper).onClose(asUncheckedRunnable(cursor));
    }

    /**
     * Read all rows of a SAS dataset.
     *
     * @param <T>
     * @param file the SAS dataset to read
     * @param rowMapper
     * @return a non-null list
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public <T> List<T> getAllRows(@NonNull Path file, @NonNull SasRowMapper<T> rowMapper) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(rowMapper);
        try (SasForwardCursor cursor = readForward(file)) {
            List<T> result = new ArrayList<>(cursor.getRowCount());
            while (cursor.next()) {
                result.add(rowMapper.apply(cursor));
            }
            return result;
        }
    }

    private SasReader getReader() throws IOException {
        return reader.orElseThrow(() -> new IOException("No reader available"));
    }

    private static <T> Stream<T> streamOf(SasForwardCursor rs, SasRowMapper<T> rowMapper) throws IOException {
        return StreamSupport.stream(spliteratorOf(rs, rowMapper), false);
    }

    private static <T> Spliterator<T> spliteratorOf(SasForwardCursor cursor, SasRowMapper<T> rowMapper) throws IOException {
        return Spliterators.spliterator(new SasIterator<>(cursor, rowMapper), cursor.getRowCount(), Spliterator.ORDERED | Spliterator.NONNULL);
    }

    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @lombok.RequiredArgsConstructor
    private static final class SasIterator<T> implements Iterator<T> {

        @lombok.NonNull
        private final SasForwardCursor cursor;

        @lombok.NonNull
        private final SasRowMapper<T> func;

        private T row = null;
        private boolean rowLoaded = false;

        @Override
        public boolean hasNext() {
            if (rowLoaded) {
                return true;
            }
            try {
                if (cursor.next()) {
                    row = func.apply(cursor);
                    return rowLoaded = true;
                }
                row = null;
                return rowLoaded = false;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public T next() {
            if (hasNext()) {
                rowLoaded = false;
                return row;
            }
            throw new NoSuchElementException();
        }
    }
}
