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
import java.nio.file.Path;
import java.util.Set;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class EOFReader implements SasReader {

    @lombok.NonNull
    private final SasReader delegate;

    @lombok.NonNull
    private final Opts opts;

    @lombok.NonNull
    private final EOFCursor.Opts cursorOpts;

    @lombok.NonNull
    private final EOFForward.Opts forwardOpts;

    @lombok.NonNull
    private final EOFScrollable.Opts scrollableOpts;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public int getCost() {
        return delegate.getCost();
    }

    @Override
    public Set<SasFeature> getFeatures() {
        return delegate.getFeatures();
    }

    @Override
    public SasForwardCursor readForward(Path file) throws IOException {
        if (opts.isAllowReadForward()) {
            return new EOFForward(delegate.readForward(file), cursorOpts, forwardOpts);
        }
        throw new EOFException("readForward");
    }

    @Override
    public SasScrollableCursor readScrollable(Path file) throws IOException {
        if (opts.isAllowReadScrollable()) {
            return new EOFScrollable(delegate.readScrollable(file), cursorOpts, scrollableOpts);
        }
        throw new EOFException("readScrollable");
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        if (opts.isAllowReadMetaData()) {
            return delegate.readMetaData(file);
        }
        throw new EOFException("readMetaData");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Opts {

        public static final Opts NONE = Opts.builder().build();
        public static final Opts ALL = Opts.builder().allowReadForward(true).allowReadScrollable(true).allowReadMetaData(true).build();

        private boolean allowReadForward;
        private boolean allowReadScrollable;
        private boolean allowReadMetaData;
    }
}
