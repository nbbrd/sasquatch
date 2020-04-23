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
package sasquatch.ri;

import internal.ri.data.Document;
import internal.ri.data.rows.RowCursor;
import internal.ri.data.rows.ValueReader;
import internal.ri.data.rows.ValueReader.DateReader;
import internal.ri.data.rows.ValueReader.DateTimeReader;
import internal.ri.data.rows.ValueReader.NumberReader;
import internal.ri.data.rows.ValueReader.StringReader;
import internal.ri.data.rows.ValueReader.TimeReader;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import sasquatch.SasColumn;
import static sasquatch.SasColumnType.*;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
final class SasquatchCursor implements SasForwardCursor {

    static SasquatchCursor of(SeekableByteChannel sbc) throws IOException {
        Document doc = Document.parse(sbc);
        SasMetaData metaData = DocumentUtil.getMetaData(doc);
        RowCursor rowCursor = RowCursor.of(sbc, doc);
        ValueReader[] readers = createReaders(metaData.getColumns(), DocumentUtil.getOffsets(doc), DocumentUtil.getCharset(doc));
        return new SasquatchCursor(metaData, rowCursor, readers, sbc);
    }

    private final SasMetaData metaData;
    private final RowCursor rowCursor;
    private final ValueReader[] readers;
    private final SeekableByteChannel resource;

    @Override
    public SasMetaData getMetaData() {
        return metaData;
    }

    @Override
    public boolean next() throws IOException {
        return rowCursor.next();
    }

    @Override
    public Object getValue(int columnIndex) throws IOException, IndexOutOfBoundsException {
        return readers[columnIndex].read(rowCursor.getBytes());
    }

    @Override
    public double getNumber(int columnIndex) throws IOException {
        try {
            return ((NumberReader) readers[columnIndex]).readDouble(rowCursor.getBytes());
        } catch (ClassCastException ex) {
            throw invalidColumnType(columnIndex, NumberReader.class, ex);
        }
    }

    @Override
    public String getString(int columnIndex) throws IOException {
        try {
            return ((StringReader) readers[columnIndex]).read(rowCursor.getBytes());
        } catch (ClassCastException ex) {
            throw invalidColumnType(columnIndex, StringReader.class, ex);
        }
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException {
        try {
            return ((DateReader) readers[columnIndex]).read(rowCursor.getBytes());
        } catch (ClassCastException ex) {
            throw invalidColumnType(columnIndex, DateReader.class, ex);
        }
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException {
        try {
            return ((DateTimeReader) readers[columnIndex]).read(rowCursor.getBytes());
        } catch (ClassCastException ex) {
            throw invalidColumnType(columnIndex, DateTimeReader.class, ex);
        }
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException {
        try {
            return ((TimeReader) readers[columnIndex]).read(rowCursor.getBytes());
        } catch (ClassCastException ex) {
            throw invalidColumnType(columnIndex, TimeReader.class, ex);
        }
    }

    @Override
    public Object[] getValues() throws IOException {
        Object[] result = new Object[readers.length];
        for (int j = 0; j < result.length; j++) {
            result[j] = getValue(j);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        resource.close();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private IllegalArgumentException invalidColumnType(int columnIndex, Class<? extends ValueReader> expected, ClassCastException ex) {
        Class<? extends ValueReader> actual = readers[columnIndex].getClass();
        return new IllegalArgumentException("Column at index " + columnIndex + " expected to be '" + getName(expected) + "' but was '" + getName(actual) + "' instead");
    }

    private static String getName(Class<? extends ValueReader> reader) {
        if (NumberReader.class.isAssignableFrom(reader)) {
            return "Number";
        }
        if (StringReader.class.isAssignableFrom(reader)) {
            return "String";
        }
        if (DateReader.class.isAssignableFrom(reader)) {
            return "Date";
        }
        if (DateTimeReader.class.isAssignableFrom(reader)) {
            return "DateTime";
        }
        if (TimeReader.class.isAssignableFrom(reader)) {
            return "Time";
        }
        throw new RuntimeException("Unknown column type");
    }

    private static ValueReader[] createReaders(List<SasColumn> columns, int[] offsets, Charset charset) {
        ValueReader[] result = new ValueReader[columns.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = createReader(columns.get(i), offsets[i], charset);
        }
        return result;
    }

    private static ValueReader createReader(SasColumn c, int offset, Charset charset) {
        switch (c.getType()) {
            case CHARACTER:
                return ValueReader.stringReader(offset, c.getLength(), charset);
            case NUMERIC:
                return ValueReader.numberReader(offset, c.getLength());
            case DATE:
                return ValueReader.dateReader(ValueReader.numberReader(offset, c.getLength()));
            case DATETIME:
                return ValueReader.dateTimeReader(ValueReader.numberReader(offset, c.getLength()));
            case TIME:
                return ValueReader.timeReader(ValueReader.numberReader(offset, c.getLength()));
        }
        throw new RuntimeException("Unknown column type");
    }
    //</editor-fold>
}
