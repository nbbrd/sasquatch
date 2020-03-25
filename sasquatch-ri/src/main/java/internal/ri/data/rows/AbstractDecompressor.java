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
package internal.ri.data.rows;

import internal.bytes.BytesReader;
import internal.bytes.BytesWriter;

/**
 *
 * @author Philippe Charles
 */
abstract class AbstractDecompressor implements Decompressor {

    static void copy(BytesReader src, int srcPos, BytesWriter dst, int dstPos, int length) {
        src.copyTo(srcPos, dst, dstPos, length);
    }

    static void insert(BytesWriter dst, int dstPos, int length, byte value) {
        dst.fill(dstPos, dstPos + length, value);
    }

    static int uint12(byte low, byte high) {
        return uint4(low) + (uint8(high) << 4);
    }

    static int uint8(byte value) {
        return value & 0xff;
    }

    static int uint4(byte value) {
        return value & 0x0f;
    }
}
