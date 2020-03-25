/*
 * Copyright 2013 National Bank of Belgium
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
import internal.ri.base.SasCalendar;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface ValueReader<T> {

    @Nullable
    T read(@NonNull BytesReader bytes);

    interface NumberReader extends ValueReader<Double> {

        @Override
        default Double read(BytesReader bytes) {
            return readDouble(bytes);
        }

        double readDouble(@NonNull BytesReader bytes);
    }

    interface StringReader extends ValueReader<String> {
    }

    interface DateReader extends ValueReader<LocalDate> {
    }

    interface DateTimeReader extends ValueReader<LocalDateTime> {
    }

    interface TimeReader extends ValueReader<LocalTime> {
    }

    @NonNull
    static NumberReader numberReader(int offset, int length) {
        switch (length) {
            case 8:
                return bytes -> bytes.getFloat64(offset);
            default:
                return new CustomNumericReader(offset, length);
        }
    }

    @NonNull
    static StringReader stringReader(int offset, int length, @NonNull Charset charset) {
        return new DecoderStringReader(offset, length, charset);
    }

    @NonNull
    static DateReader dateReader(@NonNull NumberReader reader) {
        return bytes -> SasCalendar.getDate(reader.readDouble(bytes));
    }

    @NonNull
    static DateTimeReader dateTimeReader(@NonNull NumberReader reader) {
        return bytes -> SasCalendar.getDateTime(reader.readDouble(bytes));
    }

    @NonNull
    static TimeReader timeReader(@NonNull NumberReader reader) {
        return bytes -> SasCalendar.getTime(reader.readDouble(bytes));
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    static final class DecoderStringReader implements StringReader {

        private final int offset;
        private final int length;
        private final CharsetDecoder decoder;

        private DecoderStringReader(int offset, int length, Charset charset) {
            this.offset = offset;
            this.length = length;
            this.decoder = charset.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
        }

        @Override
        public String read(BytesReader bytes) {
            try {
                String result = bytes.decode(offset, length, decoder).toString().trim();
                return result.isEmpty() ? null : result;
            } catch (CharacterCodingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    static final class DefaultStringReader implements StringReader {

        private final int offset;
        private final byte[] buffer;
        private final Charset charset;

        private DefaultStringReader(int offset, int length, Charset charset) {
            this.offset = offset;
            this.buffer = new byte[length];
            this.charset = charset;
        }

        @Override
        public String read(BytesReader bytes) {
            bytes.copyTo(offset, buffer, 0, buffer.length);
            return new String(buffer, charset).trim();
        }
    }

    static final class CustomNumericReader implements NumberReader {

        private final int offset;
        private final int start;
        private final ByteBuffer tmp;

        private CustomNumericReader(int offset, int length) {
            this.offset = offset;
            this.start = 8 - length;
            this.tmp = ByteBuffer.wrap(new byte[8]);
        }

        @Override
        public double readDouble(BytesReader bytes) {
            for (int i = start; i < 8; i++) {
                tmp.put(i, bytes.getByte(offset + i - start));
            }
            return tmp.order(bytes.getOrder()).getDouble(0);
        }
    }
    //</editor-fold>
}
