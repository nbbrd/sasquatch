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
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import sasquatch.SasMetaData;
import sasquatch.spi.SasCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class FailsafeSasReader implements SasReader {

    public static FailsafeSasReader wrap(SasReader delegate) {
        return new FailsafeSasReader(delegate, Failsafe.DEFAULT);
    }

    @lombok.Getter
    @lombok.NonNull
    private final SasReader delegate;

    @lombok.NonNull
    private final Failsafe failsafe;

    @Override
    public String getName() {
        String result;

        try {
            result = delegate.getName();
        } catch (RuntimeException unexpected) {
            return fallbackError("getName", unexpected, getSource().getName());
        }

        if (result == null) {
            return fallbackNull("getName", getSource().getName());
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        try {
            return delegate.isAvailable();
        } catch (RuntimeException unexpected) {
            return fallbackError("isAvailable", unexpected, false);
        }
    }

    @Override
    public int getCost() {
        int result;

        try {
            result = delegate.getCost();
        } catch (RuntimeException unexpected) {
            return fallbackError("getCost", unexpected, Integer.MAX_VALUE);
        }

        if (result < 0) {
            return fallbackNonNegative("getCost", Integer.MAX_VALUE);
        }

        return result;
    }

    @Override
    public Set<SasFeature> getFeatures() {
        Set<SasFeature> result;

        try {
            result = delegate.getFeatures();
        } catch (RuntimeException unexpected) {
            return fallbackError("getFeatures", unexpected, Collections.emptySet());
        }

        if (result == null) {
            return fallbackNull("getFeatures", Collections.emptySet());
        }

        return result;
    }

    @Override
    public SasCursor read(Path file) throws IOException {
        SasCursor result;

        try {
            result = delegate.read(file);
        } catch (RuntimeException unexpected) {
            throw forwardError("read", unexpected);
        }

        if (result == null) {
            throw forwardNull("read");
        }

        return new FailsafeSasCursor(result, failsafe);
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        SasMetaData result;

        try {
            result = delegate.readMetaData(file);
        } catch (RuntimeException unexpected) {
            throw forwardError("readMetaData", unexpected);
        }

        if (result == null) {
            throw forwardNull("readMetaData");
        }

        return result;
    }

    private IOException forwardError(String method, RuntimeException unexpected) {
        String msg = Failsafe.getErrorMsg(getSource(), method);
        return failsafe.forwardError(msg, unexpected, IOException::new);
    }

    private <X> X fallbackError(String method, RuntimeException unexpected, X fallback) {
        String msg = Failsafe.getErrorMsg(getSource(), method);
        return failsafe.fallbackError(msg, unexpected, fallback);
    }

    private IOException forwardNull(String method) {
        String msg = Failsafe.getNullMsg(getSource(), method);
        return failsafe.forwardValue(msg, IOException::new);
    }

    private <X> X fallbackNull(String method, X fallback) {
        String msg = Failsafe.getNullMsg(getSource(), method);
        return failsafe.fallbackValue(msg, fallback);
    }

    private <X extends Number> X fallbackNonNegative(String method, X fallback) {
        String msg = Failsafe.getNonNegativeMsg(getSource(), method);
        return failsafe.fallbackValue(msg, fallback);
    }

    private Class<?> getSource() {
        return delegate.getClass();
    }
}
