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
package sasquatch.parso;

import com.epam.parso.Column;
import com.epam.parso.ColumnFormat;
import com.epam.parso.SasFileProperties;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.ColumnUtil;
import com.epam.parso.impl.SasFileReaderImpl;
import sasquatch.SasColumn;
import sasquatch.SasMetaData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import sasquatch.SasColumnFormat;
import sasquatch.SasForwardCursor;

/**
 *
 * @author Philippe Charles
 */
final class ParsoCursor implements SasForwardCursor {

    private final InputStream stream;
    private final SasFileReader reader;
    private final SasMetaData metaData;
    private Object[] currentRow;

    public ParsoCursor(Path file) throws IOException {
        this.stream = Files.newInputStream(file);
        this.reader = new SasFileReaderImpl(stream);
        this.metaData = getMetaData(reader);
        this.currentRow = null;
    }

    @Override
    public SasMetaData getMetaData() throws IOException {
        return metaData;
    }

    @Override
    public boolean next() throws IOException {
        return (currentRow = reader.readNext()) != null;
    }

    @Override
    public Object getValue(int columnIndex) throws IOException, IndexOutOfBoundsException {
        switch (getColumns().get(columnIndex).getType()) {
            case CHARACTER:
                return currentRow[columnIndex];
            case NUMERIC:
                return currentRow[columnIndex];
            case DATE:
                return getDate(columnIndex);
            case DATETIME:
                return getDateTime(columnIndex);
            case TIME:
                return getTime(columnIndex);
        }
        throw new RuntimeException("Invalid type");
    }

    @Override
    public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            Number number = ((Number) currentRow[columnIndex]);
            return number != null ? number.doubleValue() : Double.NaN;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            return ((String) currentRow[columnIndex]);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            Date date = (Date) currentRow[columnIndex];
            return date != null ? toLocalDateTime(date).toLocalDate() : null;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            Date date = (Date) currentRow[columnIndex];
            return date != null ? toLocalDateTime(date) : null;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        try {
            Number number = (Number) currentRow[columnIndex];
            return number != null ? toLocalTime(number) : null;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Object[] getValues() throws IOException {
        Object[] result = new Object[currentRow.length];
        for (int j = 0; j < result.length; j++) {
            result[j] = getValue(j);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    private static LocalTime toLocalTime(Number number) {
        return LocalTime.MIDNIGHT.plusSeconds(number.longValue());
    }

    private static SasMetaData getMetaData(SasFileReader reader) throws IOException {
        SasFileProperties p = reader.getSasFileProperties();
        return SasMetaData.builder()
                .name(p.getName())
                .label(p.getFileLabel())
                .creationTime(toLocalDateTime(p.getDateCreated()))
                .lastModificationTime(toLocalDateTime(p.getDateModified()))
                .release(p.getSasRelease())
                .host(p.getServerType())
                .rowCount((int) p.getRowCount())
                .columns(getColumns(reader.getColumns()))
                .build();
    }

    private static List<SasColumn> getColumns(List<Column> columns) {
        SasColumn.Builder b = SasColumn.builder();
        return columns.stream().map(o -> b
                .order(o.getId() - 1)
                .name(o.getName())
                .type(ColumnUtil.getType(o.getType(), o.getFormat().getName()))
                .length(o.getLength())
                .format(getFormat(o.getFormat()))
                .label(o.getLabel())
                .build())
                .collect(Collectors.toList());
    }

    private static SasColumnFormat getFormat(ColumnFormat o) {
        return SasColumnFormat
                .builder()
                .name(o.getName())
                .width(o.getWidth())
                .precision(o.getPrecision())
                .build();
    }
}
