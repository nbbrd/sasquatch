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
package _test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;
import sasquatch.SasSplittableCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class FakeSasReader implements SasReader {

    @lombok.Getter
    private String name;

    @lombok.Getter
    private boolean available;

    @lombok.Getter
    private int cost;

    @lombok.Getter
    @lombok.Singular
    private Set<SasFeature> features;

    @lombok.Singular
    private Map<Path, SasArray> tables;

    private SasArray getTable(Path file) throws IOException {
        SasArray result = tables.get(file);
        if (result == null) {
            throw new IOException();
        }
        return result;
    }

    @Override
    public SasForwardCursor readForward(Path file) throws IOException {
        return getTable(file).readForward();
    }

    @Override
    public SasScrollableCursor readScrollable(Path file) throws IOException {
        return getTable(file).readScrollable();
    }

    @Override
    public SasSplittableCursor readSplittable(Path file) throws IOException {
        return getTable(file).readSplittable();
    }

    @Override
    public SasMetaData readMetaData(Path file) throws IOException {
        return getTable(file).getMetaData();
    }
}
