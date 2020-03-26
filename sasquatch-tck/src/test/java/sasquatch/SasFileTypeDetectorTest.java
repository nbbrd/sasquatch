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
package sasquatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static sasquatch.samples.SasResources.LITTLE_32;

/**
 *
 * @author Philippe Charles
 */
public class SasFileTypeDetectorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testHasSasId() throws IOException {
        assertThat(SasFileTypeDetector.hasSasId(LITTLE_32)).isTrue();

        Path emptyTxt = temp.newFile("empty.txt").toPath();
        assertThat(SasFileTypeDetector.hasSasId(emptyTxt)).isFalse();

        Path notEmptyTxt = temp.newFile("not_empty.txt").toPath();
        Files.write(notEmptyTxt, Collections.singleton("hello"), StandardCharsets.UTF_8);
        assertThat(SasFileTypeDetector.hasSasId(emptyTxt)).isFalse();

    }

    @Test(expected = NoSuchFileException.class)
    public void testHasSasIdOnMissingFile() throws IOException {
        Path deleted = temp.newFile("deleted.sas7bdat").toPath();
        Files.delete(deleted);
        assertThat(SasFileTypeDetector.hasSasId(deleted)).isFalse();
    }
}
