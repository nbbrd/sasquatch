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
import java.util.List;
import sasquatch.SasColumn;
import sasquatch.SasCursor;
import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.RequiredArgsConstructor
public abstract class EOFCursor<T extends SasCursor> implements SasCursor {

    @lombok.NonNull
    protected final T delegate;

    @lombok.NonNull
    private final Opts opts;

    @Override
    public SasMetaData getMetaData() throws IOException {
        if (opts.isAllowGetMetaData()) {
            return delegate.getMetaData();
        }
        throw new EOFException("getMetaData");
    }

    @Override
    public int getRowCount() throws IOException {
        if (opts.isAllowGetRowCount()) {
            return delegate.getRowCount();
        }
        throw new EOFException("getRowCount");
    }

    @Override
    public List<SasColumn> getColumns() throws IOException {
        if (opts.isAllowGetColumns()) {
            return delegate.getColumns();
        }
        throw new EOFException("allowGetColumns");
    }

    @Override
    public void close() throws IOException {
        if (opts.isAllowClose()) {
            delegate.close();
        }
        throw new EOFException("close");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Opts {

        public static final Opts NONE = Opts.builder().build();
        public static final Opts META = Opts
                .builder()
                .allowGetMetaData(true)
                .allowGetRowCount(true)
                .allowGetColumns(true)
                .build();

        private boolean allowGetMetaData;
        private boolean allowGetRowCount;
        private boolean allowGetColumns;
        private boolean allowClose;
    }
}
