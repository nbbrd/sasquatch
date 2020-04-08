/*
 * Copyright 2017 National Bank of Belgium
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

import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public interface BytesReader {

    int getLength();

    byte getByte(int index);

    default byte getLow(int index) {
        return (byte) (getByte(index) & 0x0F);
    }

    default byte getHigh(int index) {
        return (byte) ((getByte(index) >> 4) & 0x0F);
    }

    @NonNull
    byte[] getBytes(int index, int length);

    float getFloat32(int index);

    double getFloat64(int index);

    short getInt16(int index);

    int getInt32(int index);

    long getInt64(int index);

    default short getUInt8(int index) {
        return (short) (getByte(index) & 0xff);
    }

    default int getUInt16(int index) {
        return getInt16(index) & 0xffff;
    }

    @NonNull
    String getString(int index, int length, @NonNull Charset charset);

    @NonNull
    default byte[] toArray() {
        byte[] result = new byte[getLength()];
        copyTo(0, result, 0, result.length);
        return result;
    }

    void copyTo(int index, @NonNull byte[] dst, int dstPos, int length);

    default void copyTo(int index, @NonNull BytesWriter dst, int dstPos, int length) {
        for (int i = 0; i < length; i++) {
            dst.putByte(dstPos + i, getByte(index + i));
        }
    }

    @NonNull
    BytesReader slice(int index, int length);

    @NonNull
    CharBuffer decode(int index, int length, @NonNull CharsetDecoder decoder) throws CharacterCodingException;

    @NonNull
    ByteOrder getOrder();

    @NonNull
    BytesReader duplicate(@NonNull ByteOrder order);
}
