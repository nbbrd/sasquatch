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
package sasquatch.sassy;

import sasquatch.SasMetaData;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import sasquatch.SasResultSet;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class SassyResultSet implements SasResultSet {

    private final SasMetaData metaData;
    private final List<Object[]> data;
    private int cursor = -1;

    @Override
    public SasMetaData getMetaData() throws IOException {
        return metaData;
    }

    @Override
    public boolean nextRow() throws IOException {
        if (cursor + 1 >= data.size()) {
            return false;
        }
        cursor++;
        return true;
    }

    @Override
    public Object getValue(int columnIndex) throws IOException {
        return data.get(cursor)[columnIndex];
    }

    @Override
    public double getNumber(int columnIndex) throws IOException {
        return ((Number) getValue(columnIndex)).doubleValue();
    }

    @Override
    public String getString(int columnIndex) throws IOException {
        return (String) getValue(columnIndex);
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        throw new IOException("Not supported yet.");
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException {
        throw new IOException("Not supported yet.");
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        throw new IOException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
    }
}
