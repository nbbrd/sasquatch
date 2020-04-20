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
package sasquatch.biostatmatt;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class BiostatmattCursor implements SasForwardCursor, SasScrollableCursor {

    @lombok.NonNull
    private final SasMetaData metaData;

    @lombok.NonNull
    private final RUtils.RList<RUtils.RVector<Object>> data;

    private int row = -1;

    @Override
    public SasMetaData getMetaData() throws IOException {
        return metaData;
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
        return 0 <= row && row < getRowCount();
    }

    @Override
    public Object getValue(int columnIndex) throws IOException, IndexOutOfBoundsException {
        return data.get(columnIndex + 1).get(row + 1);
    }

    @Override
    public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return (double) getValue(columnIndex);
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return (String) getValue(columnIndex);
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return (LocalDate) getValue(columnIndex);
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return (LocalDateTime) getValue(columnIndex);
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return (LocalTime) getValue(columnIndex);
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
    }
}
