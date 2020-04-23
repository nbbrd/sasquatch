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
import sasquatch.SasScrollableCursor;

/**
 *
 * @author Philippe Charles
 */
public final class EOFScrollable extends EOFRowCursor<SasScrollableCursor> implements SasScrollableCursor {

    @lombok.NonNull
    private final Opts opts;

    public EOFScrollable(SasScrollableCursor delegate, EOFCursor.Opts cursor, EOFRowCursor.Opts row, Opts opts) {
        super(delegate, cursor, row);
        this.opts = opts;
    }

    @Override
    public int getRow() throws IOException {
        if (opts.isAllowGetRow()) {
            return delegate.getRow();
        }
        throw new EOFException("getRow");
    }

    @Override
    public boolean moveTo(int row) throws IOException {
        if (opts.isAllowMoveTo()) {
            return delegate.moveTo(row);
        }
        throw new EOFException("moveTo");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Opts {

        public static final Opts NONE = Opts.builder().build();

        private boolean allowGetRow;
        private boolean allowMoveTo;
    }
}
