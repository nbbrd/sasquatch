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

import java.io.EOFException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import sasquatch.SasMetaData;
import sasquatch.spi.SasCursor;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class EOFCursor implements SasCursor {

    @lombok.NonNull
    private final SasCursor delegate;

    @lombok.NonNull
    private final Behavior behavior;

    @Override
    public boolean nextRow() throws IOException {
        if (behavior.allowNextRow) {
            return delegate.nextRow();
        }
        throw new EOFException("nextRow");
    }

    @Override
    public SasMetaData getMetaData() throws IOException {
        if (behavior.allowGetMetaData) {
            return delegate.getMetaData();
        }
        throw new EOFException("getMetaData");
    }

    @Override
    public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (behavior.allowGetNumber) {
            return delegate.getNumber(columnIndex);
        }
        throw new EOFException("getNumber");
    }

    @Override
    public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (behavior.allowGetString) {
            return delegate.getString(columnIndex);
        }
        throw new EOFException("getString");
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (behavior.allowGetDate) {
            return delegate.getDate(columnIndex);
        }
        throw new EOFException("getDate");
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (behavior.allowGetDateTime) {
            return delegate.getDateTime(columnIndex);
        }
        throw new EOFException("getDateTime");
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (behavior.allowGetTime) {
            return delegate.getTime(columnIndex);
        }
        throw new EOFException("getTime");
    }

    @Override
    public void close() throws IOException {
        if (behavior.allowClose) {
            delegate.close();
        }
        throw new EOFException("close");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Behavior {

        public static final Behavior NONE = Behavior.builder().build();

        private boolean allowNextRow;
        private boolean allowGetMetaData;
        private boolean allowGetNumber;
        private boolean allowGetString;
        private boolean allowGetDate;
        private boolean allowGetDateTime;
        private boolean allowGetTime;
        private boolean allowClose;
    }
}
