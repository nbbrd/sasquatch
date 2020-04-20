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
package sasquatch.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class SasArray {

    @lombok.Getter
    @lombok.NonNull
    private SasMetaData metaData;

    @lombok.NonNull
    private List<Object[]> rows;

    @Nullable
    public Object getValue(int row, int column) {
        return rows.get(row)[column];
    }

    @NonNull
    public SasForwardCursor readForward() {
        return new SasArrayCursor();
    }

    @NonNull
    public SasScrollableCursor readScrollable() {
        return new SasArrayCursor();
    }

    @NonNull
    public static SasArray copyOf(@NonNull SasForwardCursor cursor) throws IOException {
        List<Object[]> result = new ArrayList<>(cursor.getRowCount());
        while (cursor.next()) {
            Object[] row = new Object[cursor.getColumns().size()];
            for (int j = 0; j < row.length; j++) {
                row[j] = cursor.getValue(j);
            }
            result.add(row);
        }
        return SasArray.of(cursor.getMetaData(), result);
    }

    private final class SasArrayCursor implements SasForwardCursor, SasScrollableCursor {

        private int row = -1;

        private <T> T getValue(Class<T> type, int columnIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
            try {
                return type.cast(SasArray.this.getValue(row, columnIndex));
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public SasMetaData getMetaData() throws IOException {
            return metaData;
        }

        @Override
        public Object getValue(int columnIndex) throws IOException {
            return getValue(Object.class, columnIndex);
        }

        @Override
        public double getNumber(int columnIndex) throws IOException {
            return getValue(Double.class, columnIndex);
        }

        @Override
        public String getString(int columnIndex) throws IOException {
            return getValue(String.class, columnIndex);
        }

        @Override
        public LocalDate getDate(int columnIndex) throws IOException {
            return getValue(LocalDate.class, columnIndex);
        }

        @Override
        public LocalDateTime getDateTime(int columnIndex) throws IOException {
            return getValue(LocalDateTime.class, columnIndex);
        }

        @Override
        public LocalTime getTime(int columnIndex) throws IOException {
            return getValue(LocalTime.class, columnIndex);
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public boolean next() throws IOException {
            return moveTo(getRow() + 1);
        }

        @Override
        public int getRow() throws IOException {
            return row;
        }

        @Override
        public boolean moveTo(int row) throws IOException {
            this.row = row;
            return 0 <= row && row < rows.size();
        }
    }
}
