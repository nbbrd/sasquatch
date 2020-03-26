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
package internal.ri.data;

import static internal.bytes.PValue.known;
import internal.ri.base.SubHeaderLocation;
import static internal.ri.data.ColType.CHARACTER;
import static internal.ri.data.ColType.NUMERIC;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import org.junit.Test;
import static sasquatch.samples.SasResources.BIG_32;
import static sasquatch.samples.SasResources.BIG_64;
import static sasquatch.samples.SasResources.LITTLE_32;
import static sasquatch.samples.SasResources.LITTLE_64;
import static sasquatch.samples.SasResources.LITTLE_64_CHAR;

/**
 *
 * @author Philippe Charles
 */
public class DocumentTest {

    @Test
    public void test_32_little() throws IOException {
        Document doc = Document.parse(LITTLE_32);

        assertThat(doc.getLength()).isEqualTo(5120);
        assertThat(doc.getCompression().get()).isEqualTo(Compression.NONE);

        assertThat(doc.getRowSize())
                .extracting(RowSize::getLength, RowSize::getCount, RowSize::getFirstPageMaxCount)
                .containsExactly(32, 50, 86);

        assertThat(doc.getColSize()).isEqualTo(new ColSize(new SubHeaderLocation(0, 1), 3));

        assertThat(doc.getColAttrList())
                .extracting("length", "type")
                .containsExactly(tuple(16, known(CHARACTER)), tuple(8, known(NUMERIC)), tuple(8, known(NUMERIC)));

        Charset charset = StandardCharsets.US_ASCII;

        assertThat(doc.getColumnName(0, charset)).isEqualTo("State");
        assertThat(doc.getColumnName(1, charset)).isEqualTo("EnterDate");
        assertThat(doc.getColumnName(2, charset)).isEqualTo("Size");

        assertThat(doc.getColumnLabel(0, charset)).isEqualTo("");
        assertThat(doc.getColumnLabel(1, charset)).isEqualTo("");
        assertThat(doc.getColumnLabel(2, charset)).isEqualTo("");

        assertThat(doc.getColumnFormat(0, charset)).isEqualTo("");
        assertThat(doc.getColumnFormat(1, charset)).isEqualTo("DATE");
        assertThat(doc.getColumnFormat(2, charset)).isEqualTo("");
    }

    @Test
    public void test_32_big() throws IOException {
        Document doc = Document.parse(BIG_32);

        assertThat(doc.getLength()).isEqualTo(131072);
        assertThat(doc.getCompression().get()).isEqualTo(Compression.NONE);

        assertThat(doc.getRowSize().getLength()).isEqualTo(816);
        assertThat(doc.getRowSize().getCount()).isEqualTo(10);
        assertThat(doc.getRowSize().getFirstPageMaxCount()).isEqualTo(66);

        assertThat(doc.getColSize().getCount()).isEqualTo(100);

        assertColumn(CHARACTER, 9, "Column2", "Column 2 label", "$", doc, 1);
        assertColumn(NUMERIC, 8, "Column5", "", "BEST", doc, 4);
    }

    @Test
    public void test_64_little() throws IOException {
        Document doc = Document.parse(LITTLE_64);

        assertThat(doc.getLength()).isEqualTo(131072);
        assertThat(doc.getCompression().get()).isEqualTo(Compression.NONE);

        assertThat(doc.getRowSize().getLength()).isEqualTo(816);
        assertThat(doc.getRowSize().getCount()).isEqualTo(10);
        assertThat(doc.getRowSize().getFirstPageMaxCount()).isEqualTo(62);

        assertThat(doc.getColSize().getCount()).isEqualTo(100);

        assertColumn(CHARACTER, 9, "Column2", "Column 2 label", "$", doc, 1);
        assertColumn(NUMERIC, 8, "Column5", "", "BEST", doc, 4);
    }

    @Test
    public void test_64_little_char() throws IOException {
        Document doc = Document.parse(LITTLE_64_CHAR);

        assertThat(doc.getLength()).isEqualTo(196608);
        assertThat(doc.getCompression().get()).isEqualTo(Compression.CHAR);

        assertThat(doc.getRowSize().getLength()).isEqualTo(809);
        assertThat(doc.getRowSize().getCount()).isEqualTo(10);
        assertThat(doc.getRowSize().getFirstPageMaxCount()).isEqualTo(62);

        assertThat(doc.getColSize().getCount()).isEqualTo(100);

        assertColumn(CHARACTER, 9, "Column2", "Column 2 label", "$", doc, 1);
        assertColumn(NUMERIC, 8, "Column5", "", "BEST", doc, 4);
    }

    @Test
    public void test_64_big() throws IOException {
        Document doc = Document.parse(BIG_64);

        assertThat(doc.getLength()).isEqualTo(131072);
        assertThat(doc.getCompression().get()).isEqualTo(Compression.NONE);

        assertThat(doc.getRowSize().getLength()).isEqualTo(816);
        assertThat(doc.getRowSize().getCount()).isEqualTo(10);
        assertThat(doc.getRowSize().getFirstPageMaxCount()).isEqualTo(62);

        assertThat(doc.getColSize().getCount()).isEqualTo(100);

        assertColumn(CHARACTER, 9, "Column2", "Column 2 label", "$", doc, 1);
        assertColumn(NUMERIC, 8, "Column5", "", "BEST", doc, 4);
    }

    private static void assertColumn(ColType type, int length, String name, String label, String format, Document doc, int columnIndex) {
        assertThat(doc.getColAttrList().get(columnIndex).getType().get()).isEqualTo(type);
        assertThat(doc.getColAttrList().get(columnIndex).getLength()).isEqualTo(length);
        Charset charset = StandardCharsets.US_ASCII;
        assertThat(doc.getColumnName(columnIndex, charset)).isEqualTo(name);
        assertThat(doc.getColumnLabel(columnIndex, charset)).isEqualTo(label);
        assertThat(doc.getColumnFormat(columnIndex, charset)).isEqualTo(format);
    }
}
