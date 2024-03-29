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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static sasquatch.samples.SasResources.PPHAM27;

/**
 *
 * @author Philippe Charles
 */
public class SasquatchCursorTest {

    @Test
    public void testReadWhenCountLowerThanFirstPageCount() throws IOException {
        Object[][] data = readAll(PPHAM27.getRoot().resolve("agents.sas7bdat"));
        Assertions.assertEquals(null, (String) data[0][0]);
        Assertions.assertEquals("Auckland, New Zealand", (String) data[0][1]);
        Assertions.assertEquals("Missouri", (String) data[9][0]);
        Assertions.assertEquals("Kansas City, USA", (String) data[9][1]);
    }

    @Test
    public void testReadLotsOfPages() throws IOException {
        Object[][] data = readAll(PPHAM27.getRoot().resolve("drugtest.sas7bdat"));
        Assertions.assertEquals(2d, (Double) data[0][0], 0);
        Assertions.assertEquals(0d, (Double) data[0][12], 0);
        Assertions.assertEquals(2d, (Double) data[9096][0], 0);
        Assertions.assertEquals(1d, (Double) data[9096][12], 0);
    }

    static SasquatchCursor of(Path file) throws IOException {
        return SasquatchCursor.of(Files.newByteChannel(file, READ));
    }

    static Object[][] readAll(Path file) throws IOException {
        try (SasquatchCursor cursor = of(file)) {
            int rowCount = cursor.getRowCount();
            int colCount = cursor.getColumns().size();
            Object[][] result = new Object[rowCount][colCount];
            int i = 0;
            while (cursor.next()) {
                for (int j = 0; j < colCount; j++) {
                    result[i][j] = cursor.getValue(j);
                }
                i++;
            }
            return result;
        }
    }
}
