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
package internal.sasquatch.spi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.Files;
import org.junit.Test;
import sasquatch.SasMetaData;
import sasquatch.SasResultSet;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeSasReaderTest {

    @Test
    public void testName() {
        Map<String, RuntimeException> errors = new HashMap<>();
        List<String> values = new ArrayList<>();

        FailsafeSasReader reader = new FailsafeSasReader(new FailingSasReader(), new Failsafe(errors::put, values::add));

        assertThat(reader.getName()).isEqualTo(FailingSasReader.class.getName());
        assertThat(errors).containsKey("Unexpected error while calling 'getName' on 'internal.sasquatch.spi.FailsafeSasReaderTest$FailingSasReader'");
        assertThat(values).isEmpty();
    }

    @Test
    public void testRead() {
        Map<String, RuntimeException> errors = new HashMap<>();
        List<String> values = new ArrayList<>();

        FailsafeSasReader reader = new FailsafeSasReader(new FailingSasReader(), new Failsafe(errors::put, values::add));

        assertThatIOException()
                .isThrownBy(() -> reader.read(Files.newTemporaryFile().toPath()))
                .withCauseExactlyInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).containsKey("Unexpected error while calling 'read' on 'internal.sasquatch.spi.FailsafeSasReaderTest$FailingSasReader'");
        assertThat(values).isEmpty();
    }

    static class FailingSasReader implements SasReader {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAvailable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getCost() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<SasFeature> getFeatures() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SasResultSet read(Path file) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SasMetaData readMetaData(Path file) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
