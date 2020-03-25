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
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import sasquatch.SasResultSet;
import static sasquatch.samples.SasResources.PPHAM27;

/**
 *
 * @author Philippe Charles
 */
public class SasquatchResultSetTest {

    @Test
    public void testReadWhenCountLowerThanFirstPageCount() throws IOException {
        Object[][] data = readAll(PPHAM27.getRoot().resolve("agents.sas7bdat"));
        assertEquals(null, (String) data[0][0]);
        assertEquals("Auckland, New Zealand", (String) data[0][1]);
        assertEquals("Missouri", (String) data[9][0]);
        assertEquals("Kansas City, USA", (String) data[9][1]);
    }

    @Test
    public void testReadLotsOfPages() throws IOException {
        Object[][] data = readAll(PPHAM27.getRoot().resolve("drugtest.sas7bdat"));
        assertEquals(2d, (Double) data[0][0], 0);
        assertEquals(0d, (Double) data[0][12], 0);
        assertEquals(2d, (Double) data[9096][0], 0);
        assertEquals(1d, (Double) data[9096][12], 0);
    }

    static SasquatchResultSet of(Path file) throws IOException {
        return SasquatchResultSet.of(Files.newByteChannel(file, READ));
    }

    static Object[][] readAll(Path file) throws IOException {
        try (SasResultSet rs = of(file)) {
            int rowCount = rs.getMetaData().getRowCount();
            int colCount = rs.getMetaData().getColumns().size();
            Object[][] result = new Object[rowCount][colCount];
            int i = 0;
            while (rs.nextRow()) {
                for (int j = 0; j < colCount; j++) {
                    result[i][j] = rs.getValue(j);
                }
                i++;
            }
            return result;
        }
    }
}
