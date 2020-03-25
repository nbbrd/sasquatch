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
package internal.ri.data;

import internal.bytes.PValue;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum Compression {

    NONE(""), CHAR("SASYZCRL"), BIN("SASYZCR2");

    private final String id;

    @NonNull
    public static PValue<Compression, String> tryParse(@NonNull String value, short lcs) {
        for (Compression o : Compression.values()) {
            if (o.id.equals(value)) {
                return PValue.known(o);
            }
        }
        if (lcs > 0) {
            return PValue.known(NONE);
        }
        return PValue.unknown(value);
    }
}
