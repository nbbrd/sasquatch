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
    static final byte COPY_64 = 0x00;
    static final byte INSERT_BLANK_17 = 0x06;
    static final byte INSERT_ZERO_17 = 0x07;
    static final byte COPY_1 = 0x08;
    static final byte COPY_17 = 0x09;
    static final byte COPY_33 = 0x0A;
    static final byte COPY_49 = 0x0B;
    static final byte INSERT_BYTE_3 = 0x0C;
    static final byte INSERT_AT_2 = 0x0D;
    static final byte INSERT_BLANK_2 = 0x0E;
    static final byte INSERT_ZERO_2 = 0x0F;
    // values
    static final byte ZERO = 0x00;
    static final byte BLANK = 0x20;
    static final byte AT = 0x40;

    @Override
    public void uncompress(BytesReader src, int position, Bytes dst, int length) {
        int srcPos = position;
        int dstPos = 0;

        while (srcPos < position + length) {
            int firstByteIndex = srcPos++;
            switch (src.getHigh(firstByteIndex)) {
                case COPY_64: {
                    int copyLength = 64 + uint8(src.getByte(srcPos++));
                    copy(src, srcPos, dst, dstPos, copyLength);
                    srcPos += copyLength;
                    dstPos += copyLength;
                    break;
                }
                case INSERT_BLANK_17: {
                    int insertLength = 17 + uint8(src.getByte(srcPos++));
                    insert(dst, dstPos, insertLength, BLANK);
                    dstPos += insertLength;
                    break;
                }
                case INSERT_ZERO_17: {
                    int insertLength = 17 + uint8(src.getByte(srcPos++));
                    insert(dst, dstPos, insertLength, ZERO);
                    dstPos += insertLength;
                    break;
                }
                case COPY_1: {
                    int copyLength = 1 + uint4(src.getLow(firstByteIndex));
                    copy(src, srcPos, dst, dstPos, copyLength);
                    srcPos += copyLength;
                    dstPos += copyLength;
                    break;
                }
                case COPY_17: {
                    int copyLength = 17 + uint4(src.getLow(firstByteIndex));
                    copy(src, srcPos, dst, dstPos, copyLength);
                    srcPos += copyLength;
                    dstPos += copyLength;
                    break;
                }
                case COPY_33: {
                    int copyLength = 33 + uint4(src.getLow(firstByteIndex));
                    copy(src, srcPos, dst, dstPos, copyLength);
                    srcPos += copyLength;
                    dstPos += copyLength;
                    break;
                }
                case COPY_49: {
                    int copyLength = 49 + uint4(src.getLow(firstByteIndex));
                    copy(src, srcPos, dst, dstPos, copyLength);
                    srcPos += copyLength;
                    dstPos += copyLength;
                    break;
                }
                case INSERT_BYTE_3: {
                    int insertLength = 3 + uint4(src.getLow(firstByteIndex));
                    insert(dst, dstPos, insertLength, src.getByte(srcPos++));
                    dstPos += insertLength;
                    break;
                }
                case INSERT_AT_2: {
                    int insertLength = 2 + uint4(src.getLow(firstByteIndex));
                    insert(dst, dstPos, insertLength, AT);
                    dstPos += insertLength;
                    break;
                }
                case INSERT_BLANK_2: {
                    int insertLength = 2 + uint4(src.getLow(firstByteIndex));
                    insert(dst, dstPos, insertLength, BLANK);
                    dstPos += insertLength;
                    break;
                }
                case INSERT_ZERO_2: {
                    int insertLength = 2 + uint4(src.getLow(firstByteIndex));
                    insert(dst, dstPos, insertLength, ZERO);
                    dstPos += insertLength;
                    break;
                }
                default:
                //                        throw new RuntimeException("Unknown control byte: " + (controlByte & 0xF0));
            }
        }
    }
}
