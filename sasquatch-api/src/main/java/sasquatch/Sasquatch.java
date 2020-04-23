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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sasquatch.spi.SasReader;
import sasquatch.spi.SasReaderLoader;
import sasquatch.util.SasCursors;

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
     * Read a SAS dataset into a splittable cursor.
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
    public SasSplittableCursor readSplittable(@NonNull Path file) throws IOException {
        Objects.requireNonNull(file);
        return getReader().readSplittable(file);
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
     * @param factory
     * @return a non-null stream
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public <T> Stream<T> rows(@NonNull Path file, SasRow.@NonNull Factory<T> factory) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(factory);
        SasSplittableCursor cursor = getReader().readSplittable(file);
        SasRow.Mapper<T> mapper = factory.get(cursor);
        return StreamSupport.stream(cursor.getSpliterator(), true)
                .map(asUnchecked(mapper))
                .onClose(asUnchecked(cursor));
    }

    /**
     * Read all rows of a SAS dataset.
     *
     * @param <T>
     * @param file the SAS dataset to read
     * @param factory
     * @return a non-null list
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    public <T> List<T> getAllRows(@NonNull Path file, SasRow.@NonNull Factory<T> factory) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(factory);
        try (SasForwardCursor cursor = readForward(file)) {
            SasRow.Mapper<T> mapper = factory.get(cursor);
            return SasCursors.toList(cursor, mapper);
        }
    }

    private SasReader getReader() throws IOException {
        return reader.orElseThrow(() -> new IOException("No reader available"));
    }

    private static <T> Function<SasRow, T> asUnchecked(SasRow.Mapper<T> mapper) {
        return row -> {
            try {
                return mapper.apply(row);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };
    }

    private static Runnable asUnchecked(Closeable resource) {
        return () -> {
            try {
                resource.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
