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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sasquatch.util.SasFileTypeDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static sasquatch.samples.SasResources.LITTLE_32;

/**
 * @author Philippe Charles
 */
public class SasFileTypeDetectorTest {

    @Test
    public void testHasSasId(@TempDir Path temp) throws IOException {
        assertThat(SasFileTypeDetector.hasSasId(LITTLE_32)).isTrue();

        Path emptyTxt = Files.createFile(temp.resolve("empty.txt"));
        assertThat(SasFileTypeDetector.hasSasId(emptyTxt)).isFalse();

        Path notEmptyTxt = Files.createFile(temp.resolve("not_empty.txt"));
        Files.write(notEmptyTxt, Collections.singleton("hello"), StandardCharsets.UTF_8);
        assertThat(SasFileTypeDetector.hasSasId(emptyTxt)).isFalse();

    }

    @Test
    public void testHasSasIdOnMissingFile(@TempDir Path temp) throws IOException {
        Path deleted = Files.createFile(temp.resolve("deleted.sas7bdat"));
        Files.delete(deleted);
        assertThatExceptionOfType(NoSuchFileException.class)
                .isThrownBy(() -> SasFileTypeDetector.hasSasId(deleted));
    }
}
