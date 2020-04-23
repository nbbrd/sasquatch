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
import sasquatch.SasForwardCursor;

/**
 *
 * @author Philippe Charles
 */
final class FailsafeForwardCursor extends FailsafeRowCursor<SasForwardCursor> implements SasForwardCursor {

    public FailsafeForwardCursor(SasForwardCursor cursor, Failsafe failsafe) {
        super(cursor, failsafe);
    }

    @Override
    public boolean next() throws IOException {
        try {
            return delegate.next();
        } catch (RuntimeException unexpected) {
            throw forwardError("next", unexpected);
        }
    }
}
