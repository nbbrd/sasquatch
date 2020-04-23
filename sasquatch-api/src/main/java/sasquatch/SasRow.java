/*
 * Copyright 2020 National Bank of Belgium
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface SasRow {

    /**
     * Retrieves the value of the specified column in the current row.
     * <p>
     * The value type depends on the column type and subtype; a pure numeric
     * will return a Double, a character will return a String and a time-related
     * subtype will return a Long.
     *
     * @param columnIndex the zero-based column index
     * @return a value if available, null otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     */
    @Nullable
    Object getValue(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException;

    /**
     * Retrieves the number value of the specified column in the current row.
     *
     * @param columnIndex the zero-based column index
     * @return a number value if available, NaN otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     * @throws IllegalArgumentException if the column is not of type NUMERIC
     */
    double getNumber(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Retrieves the string value of the specified column in the current row.
     *
     * @param columnIndex the zero-based column index
     * @return a String if available, null otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     * @throws IllegalArgumentException if the column is not of type CHARACTER
     */
    @Nullable
    String getString(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Retrieves the date value of the specified column in the current row.
     *
     * @param columnIndex the zero-based column index
     * @return a date if available, null otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     * @throws IllegalArgumentException if the column is not of date-related
     * subtype
     */
    @Nullable
    LocalDate getDate(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Retrieves the date time value of the specified column in the current row.
     *
     * @param columnIndex the zero-based column index
     * @return a datetime if available, null otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     * @throws IllegalArgumentException if the column is not of datetime-related
     * subtype
     */
    @Nullable
    LocalDateTime getDateTime(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Retrieves the time value of the specified column in the current row.
     *
     * @param columnIndex the zero-based column index
     * @return a time if available, null otherwise
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     * @throws IllegalArgumentException if the column is not of time-related
     * subtype
     */
    @Nullable
    LocalTime getTime(@NonNegative int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Retrieves the values of all columns in the current row.
     *
     * @return a non-null array of nullable values
     * @throws IOException if an I/O exception occurred
     * @throws IndexOutOfBoundsException if the columnIndex is invalid
     */
    @NonNull
    Object[] getValues() throws IOException;

    @FunctionalInterface
    public interface Mapper<T> {

        @Nullable
        T apply(@NonNull SasRow row) throws IOException;
    }

    @FunctionalInterface
    public interface Factory<T> {

        @NonNull
        Mapper<T> get(@NonNull SasCursor cursor) throws IOException;
    }
}
