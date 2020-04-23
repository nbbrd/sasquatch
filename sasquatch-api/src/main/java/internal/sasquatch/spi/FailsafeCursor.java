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
import java.util.List;
import sasquatch.SasColumn;
import sasquatch.SasCursor;
import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.extern.java.Log
abstract class FailsafeCursor<T extends SasCursor> implements SasCursor {

    @lombok.Getter
    @lombok.NonNull
    protected final T delegate;

    @lombok.NonNull
    protected final Failsafe failsafe;

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
    public List<SasColumn> getColumns() throws IOException {
        try {
            return delegate.getColumns();
        } catch (RuntimeException unexpected) {
            throw forwardError("getColumns", unexpected);
        }
    }

    @Override
    public int getRowCount() throws IOException {
        try {
            return delegate.getRowCount();
        } catch (RuntimeException unexpected) {
            throw forwardError("getRowCount", unexpected);
        }
    }

    protected IOException forwardError(String method, RuntimeException unexpected) {
        String msg = Failsafe.getErrorMsg(getSource(), method);
        return failsafe.forwardError(msg, unexpected, IOException::new);
    }

    protected IOException forwardNull(String method) {
        String msg = Failsafe.getNullMsg(getSource(), method);
        return failsafe.forwardValue(msg, IOException::new);
    }

    protected Class<?> getSource() {
        return delegate.getClass();
    }
}
