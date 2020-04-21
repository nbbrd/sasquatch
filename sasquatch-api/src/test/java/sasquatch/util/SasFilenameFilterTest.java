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
package sasquatch.util;

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
public class SasFilenameFilterTest {

    @Test
    public void testAccept() throws IOException {
        SasFilenameFilter x = new SasFilenameFilter();

        assertThat(x.accept(valid.getParent().toFile(), valid.getFileName().toString()))
                .isEqualTo(true);

        assertThat(x.accept(invalid.getParent().toFile(), invalid.getFileName().toString()))
                .isEqualTo(false);

        assertThat(x.accept(missing.getParent().toFile(), missing.getFileName().toString()))
                .isEqualTo(true);
    }

    private final Path sasTestfiles = Sample.getSasTestFiles();
    private final Path valid = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "sas7bdat", "acadindx.sas7bdat"));
    private final Path invalid = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "csv", "acadindx.csv"));
    private final Path missing = sasTestfiles.resolve(Paths.get("github_dumbmatter", "test", "data", "sas7bdat", "zzz.sas7bdat"));
}
