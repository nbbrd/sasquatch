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
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import nbbrd.service.ServiceProvider;
import sasquatch.SasMetaData;
import sasquatch.spi.SasCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SasReader.class)
public final class SasquatchReader implements SasReader {

    public static final String NAME = "RI";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getCost() {
        return NATIVE;
    }

    @Override
    public Set<SasFeature> getFeatures() {
        return EnumSet.copyOf(Arrays.asList(
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
        ));
    }

    @Override
    public SasCursor read(Path file) throws IOException {
        SeekableByteChannel sbc = Files.newByteChannel(file, READ);
        try {
            return SasquatchCursor.of(sbc);
        } catch (Error | RuntimeException e) {
            try {
                sbc.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {
                }
            }
            throw e;
        }
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        return DocumentUtil.getMetaData(Document.parse(file));
    }
}
