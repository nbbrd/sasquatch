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
package internal.ri.data.rows;

import internal.bytes.Bytes;
import internal.bytes.BytesReader;

/**
 * RDC (Ross Data Compression) decompressor.
 *
 * @see
 * https://www.drdobbs.com/a-simple-data-compression-technique/184402606?pgno=2
 * @see http://www.howtodothings.com/computers/a1216-ross-data-compression.html
 * @author Philippe Charles
 */
final class RDCDecompressor extends AbstractDecompressor {

    public static final RDCDecompressor INSTANCE = new RDCDecompressor();

    @Override
    public void uncompress(BytesReader src, int position, Bytes dst, int length) {
        int ctrl_bits = 0;
        int ctrl_mask = 0;

        int srcPos = position;
        int dstPos = 0;

        while (srcPos < position + length) {
            ctrl_mask >>= 1;

            /* get new load of control bits if needed */
            if (ctrl_mask == 0) {
                ctrl_bits = ((src.getByte(srcPos) & 0xff) << 8 | (src.getByte(srcPos + 1) & 0xff));
                srcPos += 2;
                ctrl_mask = 0x8000;
            }

            if (isUncompressedChar(ctrl_bits, ctrl_mask)) {
                /* Uncompressed character */
                dst.putByte(dstPos++, src.getByte(srcPos++));
            } else {
                /* Compression code */
                int firstByteIndex = srcPos++;
                switch (src.getHigh(firstByteIndex)) {
                    // Short RLE code : two-byte code that can represent 
                    // from 3 to 18 occurrences of a character 
                    case 0: {
                        int repeat = 3 + uint4(src.getLow(firstByteIndex));
                        byte value = src.getByte(srcPos++);

                        insert(dst, dstPos, repeat, value);
                        dstPos += repeat;
                        break;
                    }
                    // Long RLE code : three-byte code that can represent 
                    // from 19 to 4114 occurrences of a character
                    case 1: {
                        int repeat = 19 + uint12(src.getLow(firstByteIndex), src.getByte(srcPos++));
                        byte value = src.getByte(srcPos++);

                        insert(dst, dstPos, repeat, value);
                        dstPos += repeat;
                        break;
                    }
                    // Long pattern code :  three-byte code that represents 
                    // a pattern of 16 to 271 characters
                    case 2: {
                        int patternOffset = 3 + uint12(src.getLow(firstByteIndex), src.getByte(srcPos++));
                        int patternLength = 16 + uint8(src.getByte(srcPos++));

                        copy(dst, dstPos - patternOffset, dst, dstPos, patternLength);
                        dstPos += patternLength;
                        break;
                    }
                    // Short pattern code : two-byte code that represents 
                    // a pattern of 3 to 15 characters
                    default: {
                        int patternOffset = 3 + uint12(src.getLow(firstByteIndex), src.getByte(srcPos++));
                        int patternLength = uint4(src.getHigh(firstByteIndex));

                        copy(dst, dstPos - patternOffset, dst, dstPos, patternLength);
                        dstPos += patternLength;
                        break;
                    }
                }
            }
        }
    }

    private static boolean isUncompressedChar(int ctrl_bits, int ctrl_mask) {
        return (ctrl_bits & ctrl_mask) == 0;
    }
}
