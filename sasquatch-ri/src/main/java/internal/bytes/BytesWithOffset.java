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

/**
 *
 * @author Philippe Charles
 */
public final class BytesWithOffset implements BytesReader {

    private BytesReader delegate;
    private int offset;

    public BytesWithOffset() {
        this.delegate = null;
        this.offset = 0;
    }

    public void reset(BytesReader delegate, int offset) {
        this.delegate = delegate;
        this.offset = offset;
    }

    public void incrementOffset(int length) {
        this.offset += length;
    }

    @Override
    public byte getByte(int index) {
        return delegate.getByte(offset + index);
    }

    @Override
    public byte[] getBytes(int index, int length) {
        return delegate.getBytes(offset + index, length);
    }

    @Override
    public double getFloat64(int index) {
        return delegate.getFloat64(offset + index);
    }

    @Override
    public float getFloat32(int index) {
        return delegate.getFloat32(offset + index);
    }

    @Override
    public int getInt32(int index) {
        return delegate.getInt32(offset + index);
    }

    @Override
    public int getLength() {
        return delegate.getLength() - offset;
    }

    @Override
    public long getInt64(int index) {
        return delegate.getInt64(offset + index);
    }

    @Override
    public short getInt16(int index) {
        return delegate.getInt16(offset + index);
    }

    @Override
    public String getString(int index, int length, Charset charset) {
        return delegate.getString(offset + index, length, charset);
    }

    @Override
    public short getUInt8(int index) {
        return delegate.getUInt8(offset + index);
    }

    @Override
    public int getUInt16(int index) {
        return delegate.getUInt16(offset + index);
    }

    @Override
    public void copyTo(int index, byte[] dst, int dstPos, int length) {
        delegate.copyTo(offset + index, dst, dstPos, length);
    }

    @Override
    public void copyTo(int index, BytesWriter dst, int dstPos, int length) {
        delegate.copyTo(offset + index, dst, dstPos, length);
    }

    @Override
    public BytesReader slice(int index, int length) {
        return delegate.slice(offset + index, length);
    }

    @Override
    public CharBuffer decode(int index, int length, CharsetDecoder decoder) throws CharacterCodingException {
        return delegate.decode(offset + index, length, decoder);
    }

    @Override
    public ByteOrder getOrder() {
        return delegate.getOrder();
    }

    @Override
    public BytesReader duplicate(ByteOrder order) {
        BytesWithOffset result = new BytesWithOffset();
        result.reset(delegate.duplicate(order), offset);
        return result;
    }
}
