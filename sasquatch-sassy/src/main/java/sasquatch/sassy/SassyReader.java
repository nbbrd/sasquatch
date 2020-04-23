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
package sasquatch.sassy;

import sasquatch.SasColumn;
import sasquatch.SasMetaData;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import nbbrd.service.ServiceProvider;
import org.eobjects.sassy.SasColumnType;
import org.eobjects.sassy.SasReaderCallback;
import sasquatch.SasColumnFormat;
import sasquatch.SasForwardCursor;
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
public final class SassyReader implements SasReader {

    @Override
    public String getName() {
        return "Sassy";
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
                SasFeature.LITTLE_ENDIAN_32,
                SasFeature.CUSTOM_NUMERIC
        );
    }

    @Override
    public SasForwardCursor readForward(Path file) throws IOException {
        DataCallback callback = new DataCallback();
        read(file, callback);
        return SasCursors.forwardOf(callback.toMetaData(), callback.data);
    }

    @Override
    public SasScrollableCursor readScrollable(Path file) throws IOException {
        DataCallback callback = new DataCallback();
        read(file, callback);
        return SasCursors.scrollableOf(callback.toMetaData(), callback.data);
    }

    @Override
    public SasSplittableCursor readSplittable(Path file) throws IOException {
        DataCallback callback = new DataCallback();
        read(file, callback);
        return SasCursors.splittableOf(callback.toMetaData(), callback.data);
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        MetaDataCallback callback = new MetaDataCallback();
        read(file, callback);
        return callback.toMetaData();
    }

    private void read(Path file, SasReaderCallback callback) throws IOException {
        try {
            new org.eobjects.sassy.SasReader(file.toFile()).read(callback);
        } catch (org.eobjects.sassy.SasReaderException ex) {
            throw new IOException(ex);
        }
    }

    private static class MetaDataCallback implements SasReaderCallback {

        private final SasColumn.Builder b = SasColumn.builder();
        protected final List<SasColumn> columns = new ArrayList<>();
        protected int rowCount = 0;

        @Override
        public void column(int index, String name, String label, SasColumnType type, int length) {
            columns.add(b
                    .order(index)
                    .name(name)
                    .type(SasColumnType.NUMERIC == type ? sasquatch.SasColumnType.NUMERIC : sasquatch.SasColumnType.CHARACTER)
                    .length(length)
                    .format(SasColumnFormat.EMPTY)
                    .label(label != null ? label : "")
                    .build());
        }

        @Override
        public boolean readData() {
            return true;
        }

        @Override
        public boolean row(int rowNumber, Object[] rowData) {
            rowCount++;
            return true;
        }

        SasMetaData toMetaData() throws IOException {
            if (columns.isEmpty()) {
                throw new IOException();
            }
            return SasMetaData.builder()
                    .rowCount(rowCount)
                    .columns(columns)
                    .build();
        }
    }

    private static final class DataCallback extends MetaDataCallback {

        private final List<Object[]> data = new ArrayList<>();

        @Override
        public boolean row(int rowNumber, Object[] rowData) {
            data.add(rowData);
            return super.row(rowNumber, rowData);
        }
    }
}
