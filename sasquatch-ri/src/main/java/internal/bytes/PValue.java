/*
 * Copyright 2016 National Bank of Belgium
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
package internal.bytes;

import java.util.Objects;
import java.util.function.Function;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @param <V>
 * @param <R>
 */
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PValue<V, R> {

    @NonNull
    public static <V, R> PValue<V, R> known(@NonNull V value) {
        return new PValue<>(value, true);
    }

    @NonNull
    public static <V, R> PValue<V, R> unknown(@NonNull R raw) {
        return new PValue<>(raw, false);
    }

    @lombok.NonNull
    private final Object obj;

    private final boolean known;

    public boolean isKnown() {
        return known;
    }

    public boolean isKnownAs(@NonNull V value) {
        return known && value.equals(obj);
    }

    public boolean isUnknown() {
        return !known;
    }

    @NonNull
    public V get() throws IllegalStateException {
        if (!known) {
            throw new IllegalStateException();
        }
        return (V) obj;
    }

    @NonNull
    public R getRaw() throws IllegalStateException {
        if (known) {
            throw new IllegalStateException();
        }
        return (R) obj;
    }

    @NonNull
    public V or(@NonNull V defaultValue) {
        return known ? (V) obj : Objects.requireNonNull(defaultValue);
    }

    @NonNull
    public <X extends Throwable> V orElseThrow(@NonNull Function<? super R, ? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);
        if (known) {
            return (V) obj;
        }
        throw exceptionSupplier.apply((R) obj);
    }

    @Override
    public String toString() {
        return known ? obj.toString() : ("=(" + obj + ")=");
    }
}
