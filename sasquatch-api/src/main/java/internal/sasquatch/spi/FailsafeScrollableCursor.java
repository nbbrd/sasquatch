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
import sasquatch.SasScrollableCursor;

/**
 *
 * @author Philippe Charles
 */
final class FailsafeScrollableCursor extends FailsafeCursor<SasScrollableCursor> implements SasScrollableCursor {

    public FailsafeScrollableCursor(SasScrollableCursor cursor, Failsafe failsafe) {
        super(cursor, failsafe);
    }

    @Override
    public int getRow() throws IOException {
        try {
            return delegate.getRow();
        } catch (RuntimeException unexpected) {
            throw forwardError("getRow", unexpected);
        }
    }

    @Override
    public boolean moveTo(int row) throws IOException {
        try {
            return delegate.moveTo(row);
        } catch (RuntimeException unexpected) {
            throw forwardError("moveTo", unexpected);
        }
    }
}
