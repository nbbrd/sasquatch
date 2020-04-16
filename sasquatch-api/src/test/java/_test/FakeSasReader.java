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
import sasquatch.spi.SasCursor;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
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
    private Map<Path, FakeSasTable> tables;

    @Override
    public SasCursor read(Path file) throws IOException {
        FakeSasTable result = tables.get(file);
        if (result == null) {
            throw new IOException();
        }
        return result.asCursor();
    }
}
