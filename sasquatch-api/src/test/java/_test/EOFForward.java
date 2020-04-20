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
import sasquatch.SasForwardCursor;

/**
 *
 * @author Philippe Charles
 */
public final class EOFForward extends EOFCursor<SasForwardCursor> implements SasForwardCursor {

    @lombok.NonNull
    private final Opts opts;

    public EOFForward(SasForwardCursor delegate, EOFCursor.Opts cursorOpts, Opts opts) {
        super(delegate, cursorOpts);
        this.opts = opts;
    }

    @Override
    public boolean next() throws IOException {
        if (opts.isAllowNext()) {
            return delegate.next();
        }
        throw new EOFException("next");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Opts {

        public static final Opts NONE = builder().build();

        private boolean allowNext;
    }
}
