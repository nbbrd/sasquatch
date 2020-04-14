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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import sasquatch.samples.KnownError;
import sasquatch.samples.SasContent;
import sasquatch.samples.SasContentLoader;
import sasquatch.spi.SasReader;
import sasquatch.tck.SasReaderAssert;

/**
 *
 * @author Philippe Charles
 */
public class SasquatchReaderTest {

    @Test
    public void testCompliance() {
        SasReaderAssert.assertCompliance(new SasquatchReader());
    }

    @Test
    public void testContent() {
        List<KnownError> knownErrors = Arrays.asList(
                new KnownError("Epam", Paths.get("charset_utf8.sas7bdat"), SasContent.HeadError.class),
                new KnownError("Epam", Paths.get("chinese_column_fails.sas7bdat"), SasContent.HeadError.class),
                new KnownError("Epam", Paths.get("chinese_column_works.sas7bdat"), SasContent.HeadError.class),
                new KnownError("Kshedden", Paths.get("test16.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Kshedden", Paths.get("test17.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Kshedden", Paths.get("test18.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Kshedden", Paths.get("test19.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Kshedden", Paths.get("test20.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Kshedden", Paths.get("test21.sas7bdat"), SasContent.BodyError.class),
                new KnownError("Dumbmatter", Paths.get("sas7bdat-unsupported", "osteo_analysis_data.sas7bdat"), SasContent.MissingError.class)
        );

        SasReader reader = new SasquatchReader();
        SasContentLoader.get().forEach(content
                -> assertThat(content.parse(reader))
                        .extracting(KnownError::of)
                        .isSubsetOf(knownErrors));
    }
}
