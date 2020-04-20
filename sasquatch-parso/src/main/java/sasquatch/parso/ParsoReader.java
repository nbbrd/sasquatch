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

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import nbbrd.service.ServiceProvider;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;
import sasquatch.util.SasArray;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SasReader.class)
public final class ParsoReader implements SasReader {

    @Override
    public String getName() {
        return "Parso";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getCost() {
        return FAST;
    }

    @Override
    public Set<SasFeature> getFeatures() {
        return EnumSet.of(
                SasFeature.ATTRIBUTES,
                SasFeature.LABEL_META,
                SasFeature.BIG_ENDIAN_32,
                SasFeature.LITTLE_ENDIAN_32,
                SasFeature.BIG_ENDIAN_64,
                SasFeature.LITTLE_ENDIAN_64,
                SasFeature.CHAR_COMP,
                SasFeature.BIN_COMP,
                SasFeature.DATE_TYPE,
                SasFeature.DATE_TIME_TYPE,
                SasFeature.TIME_TYPE,
                SasFeature.FIELD_ENCODING,
                SasFeature.COLUMN_ENCODING,
                SasFeature.CUSTOM_NUMERIC,
                SasFeature.COLUMN_FORMAT
        );
    }

    @Override
    public SasForwardCursor readForward(Path file) throws IOException {
        return new ParsoCursor(file);
    }

    @Override
    public SasScrollableCursor readScrollable(Path file) throws IOException {
        try (SasForwardCursor cursor = readForward(file)) {
            return SasArray.copyOf(cursor).readScrollable();
        }
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        try (SasForwardCursor cursor = readForward(file)) {
            return cursor.getMetaData();
        }
    }
}
