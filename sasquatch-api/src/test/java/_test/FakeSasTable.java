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
package _test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import sasquatch.SasColumn;
import sasquatch.SasMetaData;
import sasquatch.SasResultSet;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class FakeSasTable {

    @lombok.Singular
    private List<SasColumn> columns;

    @lombok.Singular
    private List<Object[]> rows;

    public SasResultSet asResultSet() {
        return new SasResultSet() {
            private int row = -1;

            private <T> T getValue(Class<T> type, int columnIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
                try {
                    return type.cast(rows.get(row)[columnIndex]);
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            @Override
            public boolean nextRow() throws IOException {
                row++;
                return row < rows.size();
            }

            @Override
            public SasMetaData getMetaData() throws IOException {
                return SasMetaData
                        .builder()
                        .columns(columns)
                        .build();
            }

            @Override
            public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
                return getValue(Double.class, columnIndex);
            }

            @Override
            public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
                return getValue(String.class, columnIndex);
            }

            @Override
            public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
                return getValue(LocalDate.class, columnIndex);
            }

            @Override
            public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
                return getValue(LocalDateTime.class, columnIndex);
            }

            @Override
            public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
                return getValue(LocalTime.class, columnIndex);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }
}
