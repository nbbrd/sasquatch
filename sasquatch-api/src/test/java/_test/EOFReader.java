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
import sasquatch.SasMetaData;
import sasquatch.spi.SasCursor;
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
    private final Behavior behavior;

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
    public SasCursor read(Path file) throws IOException {
        if (behavior.allowRead) {
            return new EOFCursor(delegate.read(file), behavior.resultSet);
        }
        throw new EOFException("read");
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        if (behavior.allowReadMetaData) {
            return delegate.readMetaData(file);
        }
        throw new EOFException("readMetaData");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Behavior {

        public static final Behavior NONE = Behavior.builder().build();

        private boolean allowRead;

        private boolean allowReadMetaData;

        @lombok.NonNull
        private EOFCursor.Behavior resultSet;

        public static BehaviorBuilder builder() {
            return new BehaviorBuilder()
                    .allowRead(false)
                    .allowReadMetaData(false)
                    .resultSet(EOFCursor.Behavior.NONE);
        }
    }
}
