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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor
final class Failsafe {

    public static final Failsafe DEFAULT = new Failsafe(Failsafe::logError, Failsafe::logValue);

    @lombok.NonNull
    private final BiConsumer<? super String, ? super RuntimeException> onUnexpectedError;

    @lombok.NonNull
    private final Consumer<? super String> onUnexpectedValue;

    public <X> X fallbackError(String msg, RuntimeException unexpected, X fallback) {
        onUnexpectedError.accept(msg, unexpected);
        return fallback;
    }

    public <X> X fallbackValue(String msg, X fallback) {
        onUnexpectedValue.accept(msg);
        return fallback;
    }

    public <X extends Exception> X forwardError(String msg, RuntimeException unexpected, BiFunction<? super String, ? super RuntimeException, X> supplier) {
        onUnexpectedError.accept(msg, unexpected);
        return supplier.apply(msg, unexpected);
    }

    public <X extends Exception> X forwardValue(String msg, Function<? super String, X> supplier) {
        onUnexpectedValue.accept(msg);
        return supplier.apply(msg);
    }

    public static String getErrorMsg(Class<?> source, String method) {
        return "Unexpected error while calling '" + method + "' on '" + source.getName() + "'";
    }

    public static String getNullMsg(Class<?> source, String method) {
        return "Unexpected null value while calling '" + method + "' on '" + source.getName() + "'";
    }

    public static String getNonNegativeMsg(Class<?> source, String method) {
        return "Unexpected negative value while calling '" + method + "' on '" + source.getName() + "'";
    }

    private static void logError(String msg, RuntimeException unexpected) {
        if (log.isLoggable(Level.WARNING)) {
            log.log(Level.WARNING, msg, unexpected);
        }
    }

    private static void logValue(String msg) {
        log.log(Level.WARNING, msg);
    }
}
