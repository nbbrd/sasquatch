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
import java.nio.file.Path;
import java.util.Iterator;
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
     * Read a SAS dataset into a result set.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The result set is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null result set
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public SasResultSet read(@NonNull Path file) throws IOException {
        Objects.requireNonNull(file);
        return getReader().read(file);
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

    @NonNull
    public <T> Stream<T> rows(@NonNull Path file, @NonNull SasRowMapper<T> rowMapper) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(rowMapper);
        SasResultSet rs = read(file);
        return streamOf(rs, rowMapper).onClose(asUncheckedRunnable(rs));
    }

    private SasReader getReader() throws IOException {
        return reader.orElseThrow(() -> new IOException("No reader available"));
    }

    private static <T> Stream<T> streamOf(SasResultSet rs, SasRowMapper<T> rowMapper) throws IOException {
        return StreamSupport.stream(spliteratorOf(rs, rowMapper), false);
    }

    private static <T> Spliterator<T> spliteratorOf(SasResultSet rs, SasRowMapper<T> rowMapper) throws IOException {
        return Spliterators.spliterator(new SasIterator<>(rs, rowMapper), rs.getMetaData().getRowCount(), Spliterator.ORDERED | Spliterator.NONNULL);
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
        private final SasResultSet rs;

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
                if (rs.nextRow()) {
                    row = func.apply(rs);
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
