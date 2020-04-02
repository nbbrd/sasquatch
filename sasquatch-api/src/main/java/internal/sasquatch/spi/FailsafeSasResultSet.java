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
package internal.sasquatch.spi;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import sasquatch.SasMetaData;
import sasquatch.SasResultSet;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.extern.java.Log
final class FailsafeSasResultSet implements SasResultSet {

    @lombok.Getter
    @lombok.NonNull
    private final SasResultSet delegate;

    @lombok.NonNull
    private final Failsafe failsafe;

    @Override
    public boolean nextRow() throws IOException {
        try {
            return delegate.nextRow();
        } catch (RuntimeException unexpected) {
            throw forwardError("nextRow", unexpected);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } catch (RuntimeException unexpected) {
            throw forwardError("close", unexpected);
        }
    }

    @Override
    public SasMetaData getMetaData() throws IOException {
        SasMetaData result;

        try {
            result = delegate.getMetaData();
        } catch (RuntimeException unexpected) {
            throw forwardError("getMetaData", unexpected);
        }

        if (result == null) {
            throw forwardNull("getMetaData");
        }

        return result;
    }

    @Override
    public Object getValue(int columnIndex) throws IOException, IndexOutOfBoundsException {
        try {
            return delegate.getValue(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            throw forwardError("getValue", unexpected);
        }
    }

    @Override
    public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return delegate.getNumber(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            if (unexpected instanceof IllegalArgumentException) {
                throw unexpected;
            }
            throw forwardError("getNumber", unexpected);
        }
    }

    @Override
    public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return delegate.getString(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            if (unexpected instanceof IllegalArgumentException) {
                throw unexpected;
            }
            throw forwardError("getString", unexpected);
        }
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return delegate.getDate(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            if (unexpected instanceof IllegalArgumentException) {
                throw unexpected;
            }
            throw forwardError("getDate", unexpected);
        }
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return delegate.getDateTime(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            if (unexpected instanceof IllegalArgumentException) {
                throw unexpected;
            }
            throw forwardError("getDateTime", unexpected);
        }
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return delegate.getTime(columnIndex);
        } catch (RuntimeException unexpected) {
            if (unexpected instanceof IndexOutOfBoundsException) {
                throw unexpected;
            }
            if (unexpected instanceof IllegalArgumentException) {
                throw unexpected;
            }
            throw forwardError("getTime", unexpected);
        }
    }

    private IOException forwardError(String method, RuntimeException unexpected) {
        String msg = Failsafe.getErrorMsg(getSource(), method);
        return failsafe.forwardError(msg, unexpected, IOException::new);
    }

    private IOException forwardNull(String method) {
        String msg = Failsafe.getNullMsg(getSource(), method);
        return failsafe.forwardValue(msg, IOException::new);
    }

    private Class<?> getSource() {
        return delegate.getClass();
    }
}
