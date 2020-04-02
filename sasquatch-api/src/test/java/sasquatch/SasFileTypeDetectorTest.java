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
package sasquatch;

import _test.Sample;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SasFileTypeDetectorTest {

    @Test
    public void testProbeContentType() throws IOException {
        SasFileTypeDetector x = new SasFileTypeDetector();

        assertThatNullPointerException()
                .isThrownBy(() -> x.probeContentType(null));

        assertThat(x.probeContentType(valid)).isNotBlank();

        assertThat(x.probeContentType(invalid)).isNull();

        assertThatIOException()
                .isThrownBy(() -> x.probeContentType(missing));

        assertThatIOException()
                .isThrownBy(() -> x.probeContentType(folder));
    }

    @Test
    public void testHasSasId() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> SasFileTypeDetector.hasSasId(null));

        assertThat(SasFileTypeDetector.hasSasId(valid)).isTrue();

        assertThat(SasFileTypeDetector.hasSasId(invalid)).isFalse();

        assertThatIOException()
                .isThrownBy(() -> SasFileTypeDetector.hasSasId(missing));

        assertThatIOException()
                .isThrownBy(() -> SasFileTypeDetector.hasSasId(folder));
    }

    private final Path sasTestfiles = Sample.getSasTestFiles();
    private final Path valid = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "sas7bdat", "acadindx.sas7bdat"));
    private final Path invalid = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "csv", "acadindx.csv"));
    private final Path missing = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "sas7bdat", "zzz.sas7bdat"));
    private final Path folder = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "sas7bdat"));
}
