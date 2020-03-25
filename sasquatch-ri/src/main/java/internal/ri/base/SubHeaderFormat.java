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
package internal.ri.base;

import internal.bytes.PValue;

/**
 *
 * @author Philippe Charles
 */
public enum SubHeaderFormat {

    PLAIN(0x0),
    TRUNCATED(0x1),
    COMPRESSED(0x4);

    private final byte value;

    SubHeaderFormat(int value) {
        this.value = (byte) value;
    }

    public static PValue<SubHeaderFormat, Byte> tryParse(byte b) {
        for (SubHeaderFormat o : SubHeaderFormat.values()) {
            if (o.value == b) {
                return PValue.known(o);
            }
        }
        return PValue.unknown(b);
    }
}
