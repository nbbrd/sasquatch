/*
 * Copyright 2016 National Bank of Belgium
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
package internal.ri.data.rows;

import internal.ri.base.Encoding;
import internal.ri.data.Document;
import internal.ri.data.rows.ValueReader.NumberReader;
import internal.ri.data.rows.ValueReader.StringReader;
import static internal.ri.data.rows.ValueReader.numberReader;
import static internal.ri.data.rows.ValueReader.stringReader;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import static java.nio.file.StandardOpenOption.READ;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static sasquatch.samples.SasResources.LITTLE_32;
import static sasquatch.samples.SasResources.LITTLE_64_CHAR;

/**
 *
 * @author Philippe Charles
 */
public class RowCursorTest {

    @Test
    public void testPackedBinaryData() throws IOException {
        StringReader col0 = stringReader(16, 16, Encoding.DEFAULT.getCharset());
        NumberReader col2 = numberReader(8, 8);
        try (SeekableByteChannel sbc = Files.newByteChannel(LITTLE_32, READ)) {
            int cpt = 0;
            RowCursor cursor = RowCursor.of(sbc, Document.parse(sbc));
            while (cursor.next()) {
                switch (cursor.getIndex()) {
                    case 0:
                        assertThat(col0.read(cursor.getBytes())).isEqualTo("Delaware");
                        assertThat(col2.readDouble(cursor.getBytes())).isEqualTo(1955d);
                        break;
                    case 49:
                        assertThat(col0.read(cursor.getBytes())).isEqualTo("Hawaii");
                        assertThat(col2.readDouble(cursor.getBytes())).isEqualTo(6423d);
                        break;
                }
                cpt++;
            }
            assertThat(cpt).isEqualTo(cursor.getCount()).isEqualTo(50);
        }
    }

    @Test
    public void testChar() throws IOException {
        StringReader col18 = stringReader(632, 9, Encoding.tryParse((short)0x1D).get().getCharset());
        NumberReader col19 = numberReader(104, 8);
        try (SeekableByteChannel sbc = Files.newByteChannel(LITTLE_64_CHAR, READ)) {
            int cpt = 0;
            RowCursor cursor = RowCursor.of(sbc, Document.parse(sbc));
            while (cursor.next()) {
                switch (cursor.getIndex()) {
                    case 0:
                        assertThat(col18.read(cursor.getBytes())).isEqualTo("crocodile");
                        assertThat(col19.readDouble(cursor.getBytes())).isEqualTo(55d);
                        break;
                    case 9:
                        assertThat(col18.read(cursor.getBytes())).isEqualTo("pear");
                        assertThat(col19.readDouble(cursor.getBytes())).isEqualTo(49d);
                        break;
                }
                cpt++;
            }
            assertThat(cpt).isEqualTo(cursor.getCount()).isEqualTo(10);
        }
    }
}
