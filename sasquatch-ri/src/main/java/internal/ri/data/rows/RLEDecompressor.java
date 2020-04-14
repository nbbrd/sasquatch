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

import internal.bytes.Bytes;
import internal.bytes.BytesReader;

/**
 * RLE (Run Length Encoding) decompressor.
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#run-length-encoding
 * @author Philippe Charles
 */
final class RLEDecompressor extends AbstractDecompressor {

    public static final RLEDecompressor INSTANCE = new RLEDecompressor();

    // commands
    static final byte COPY_64_LONG = 0x00;
    static final byte INSERT_BYTE_LONG = 0x04;
    static final byte INSERT_AT_LONG = 0x05;
    static final byte INSERT_BLANK_LONG = 0x06;
    static final byte INSERT_ZERO_LONG = 0x07;
    static final byte COPY_1_SHORT = 0x08;
    static final byte COPY_17_SHORT = 0x09;
    static final byte COPY_33_SHORT = 0x0A;
    static final byte COPY_49_SHORT = 0x0B;
    static final byte INSERT_BYTE_SHORT = 0x0C;
    static final byte INSERT_AT_SHORT = 0x0D;
    static final byte INSERT_BLANK_SHORT = 0x0E;
    static final byte INSERT_ZERO_SHORT = 0x0F;

    // values
    static final byte ZERO = 0x00;
    static final byte BLANK = 0x20;
    static final byte AT = 0x40;

    @Override
    public void uncompress(BytesReader src, int position, Bytes dst, int length) {
        int srcPos = position;
        int dstPos = 0;

        while (srcPos < position + length) {
            int firstByteIdx = srcPos++;
            switch (src.getHigh(firstByteIdx)) {
                case COPY_64_LONG: {
                    int size = 64 + uint8(src.getByte(srcPos++));
                    copy(src, srcPos, dst, dstPos, size);
                    srcPos += size;
                    dstPos += size;
                    break;
                }
                case INSERT_BYTE_LONG: {
                    int size = 17 + 1 + uint8(src.getByte(srcPos++));
                    byte value = src.getByte(srcPos++);
                    insert(dst, dstPos, size, value);
                    dstPos += size;
                    break;
                }
                case INSERT_AT_LONG: {
                    int size = 17 + uint8(src.getByte(srcPos++));
                    insert(dst, dstPos, size, AT);
                    dstPos += size;
                    break;
                }
                case INSERT_BLANK_LONG: {
                    int size = 17 + uint8(src.getByte(srcPos++));
                    insert(dst, dstPos, size, BLANK);
                    dstPos += size;
                    break;
                }
                case INSERT_ZERO_LONG: {
                    int size = 17 + uint8(src.getByte(srcPos++));
                    insert(dst, dstPos, size, ZERO);
                    dstPos += size;
                    break;
                }
                case COPY_1_SHORT: {
                    int size = 1 + (16 * 0) + uint4(src.getLow(firstByteIdx));
                    copy(src, srcPos, dst, dstPos, size);
                    srcPos += size;
                    dstPos += size;
                    break;
                }
                case COPY_17_SHORT: {
                    int size = 1 + (16 * 1) + uint4(src.getLow(firstByteIdx));
                    copy(src, srcPos, dst, dstPos, size);
                    srcPos += size;
                    dstPos += size;
                    break;
                }
                case COPY_33_SHORT: {
                    int size = 1 + (16 * 2) + uint4(src.getLow(firstByteIdx));
                    copy(src, srcPos, dst, dstPos, size);
                    srcPos += size;
                    dstPos += size;
                    break;
                }
                case COPY_49_SHORT: {
                    int size = 1 + (16 * 3) + uint4(src.getLow(firstByteIdx));
                    copy(src, srcPos, dst, dstPos, size);
                    srcPos += size;
                    dstPos += size;
                    break;
                }
                case INSERT_BYTE_SHORT: {
                    int size = 2 + 1 + uint4(src.getLow(firstByteIdx));
                    byte value = src.getByte(srcPos++);
                    insert(dst, dstPos, size, value);
                    dstPos += size;
                    break;
                }
                case INSERT_AT_SHORT: {
                    int size = 2 + uint4(src.getLow(firstByteIdx));
                    insert(dst, dstPos, size, AT);
                    dstPos += size;
                    break;
                }
                case INSERT_BLANK_SHORT: {
                    int size = 2 + uint4(src.getLow(firstByteIdx));
                    insert(dst, dstPos, size, BLANK);
                    dstPos += size;
                    break;
                }
                case INSERT_ZERO_SHORT: {
                    int size = 2 + uint4(src.getLow(firstByteIdx));
                    insert(dst, dstPos, size, ZERO);
                    dstPos += size;
                    break;
                }
                default:
                    throw new RuntimeException("Unknown control byte: " + src.getHigh(firstByteIdx));
            }
        }
    }
}
