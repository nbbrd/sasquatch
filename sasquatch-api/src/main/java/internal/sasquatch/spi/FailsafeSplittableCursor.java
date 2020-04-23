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
import java.util.Spliterator;
import sasquatch.SasRow;
import sasquatch.SasSplittableCursor;

/**
 *
 * @author Philippe Charles
 */
final class FailsafeSplittableCursor extends FailsafeCursor<SasSplittableCursor> implements SasSplittableCursor {

    public FailsafeSplittableCursor(SasSplittableCursor delegate, Failsafe failsafe) {
        super(delegate, failsafe);
    }

    @Override
    public Spliterator<SasRow> getSpliterator() throws IOException {
        Spliterator<SasRow> result;

        try {
            result = delegate.getSpliterator();
        } catch (RuntimeException ex) {
            throw forwardError("getSpliterator", ex);
        }

        if (result == null) {
            throw forwardNull("getSpliterator");
        }

        return result;
    }
}
