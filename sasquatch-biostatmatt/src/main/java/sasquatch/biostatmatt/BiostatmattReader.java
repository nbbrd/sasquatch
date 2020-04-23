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
package sasquatch.biostatmatt;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nbbrd.service.ServiceProvider;
import sasquatch.SasColumn;
import sasquatch.SasColumnType;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;
import sasquatch.SasSplittableCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;
import sasquatch.util.SasCursors;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SasReader.class)
public final class BiostatmattReader implements SasReader {

    @Override
    public String getName() {
        return "Biostatmatt";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getCost() {
        return SLOW;
    }

    @Override
    public Set<SasFeature> getFeatures() {
        return EnumSet.of(
                SasFeature.ATTRIBUTES,
                SasFeature.LITTLE_ENDIAN_32,
                SasFeature.LITTLE_ENDIAN_64
        );
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        return getMetaData(readFrame(file).getAttributes());
    }

    @Override
    public SasForwardCursor readForward(Path file) throws IOException {
        RUtils.RFrame frame = readFrame(file);
        SasMetaData meta = getMetaData(frame.getAttributes());
        List<Object[]> data = getData(meta, frame.getData());
        return SasCursors.forwardOf(meta, data);
    }

    @Override
    public SasScrollableCursor readScrollable(Path file) throws IOException {
        RUtils.RFrame frame = readFrame(file);
        SasMetaData meta = getMetaData(frame.getAttributes());
        List<Object[]> data = getData(meta, frame.getData());
        return SasCursors.scrollableOf(meta, data);
    }

    @Override
    public SasSplittableCursor readSplittable(Path file) throws IOException {
        RUtils.RFrame frame = readFrame(file);
        SasMetaData meta = getMetaData(frame.getAttributes());
        List<Object[]> data = getData(meta, frame.getData());
        return SasCursors.splittableOf(meta, data);
    }

    static RUtils.RFrame readFrame(Path file) throws IOException {
        return Sas7bdat.readSas7bdat(file.toString(), "", false, new Sas7bdat.Callback());
    }

    static SasMetaData getMetaData(Map<String, Object> attr) {
        SasMetaData.Builder result = SasMetaData.builder()
                .creationTime(getDateTime((Double) attr.get("date.created")))
                .lastModificationTime(getDateTime((Double) attr.get("date.modified")))
                .release((String) attr.get("SAS.release"))
                .host((String) attr.get("SAS.host"))
                .rowCount(((Sas7bdat.RowsInfo) attr.get("rowsInfo")).getRow_count())
                .name(((Sas7bdat.MissingHeader) attr.get("missingHeader")).getName());

        RUtils.RList<Sas7bdat.Column> columns = (RUtils.RList<Sas7bdat.Column>) attr.get("column.info");
        for (int i = 0; i < RUtils.length(columns); i++) {
            Sas7bdat.Column column = columns.get(i + 1);
            result.column(SasColumn.builder()
                    .order(i)
                    .name(nullToEmpty(column.name))
                    .label(nullToEmpty(column.label))
                    .length(column.length)
                    .type(column.type.equals(RUtils.DataType.CHARACTER) ? SasColumnType.CHARACTER : SasColumnType.NUMERIC)
                    .build());
        }

        return result.build();
    }

    static List<Object[]> getData(SasMetaData meta, RUtils.RList<RUtils.RVector<Object>> list) {
        return new AbstractList<Object[]>() {
            @Override
            public Object[] get(int row) {
                Object[] result = new Object[meta.getColumns().size()];
                for (int column = 0; column < result.length; column++) {
                    result[column] = list.get(column + 1).get(row + 1);
                }
                return result;
            }

            @Override
            public int size() {
                return meta.getRowCount();
            }
        };
    }

    private static String nullToEmpty(String o) {
        return o != null ? o : "";
    }

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    private static LocalDateTime getDateTime(double value) {
        return !Double.isNaN(value)
                ? EPOCH.plus((long) (value * 1000), ChronoUnit.MILLIS)
                : null;
    }
}
