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
package sasquatch.biostatmatt;

import org.junit.jupiter.api.Test;
import sasquatch.biostatmatt.RUtils.RFrame;
import sasquatch.biostatmatt.RUtils.RList;
import sasquatch.biostatmatt.Sas7bdat.Column;
import sasquatch.biostatmatt.Sas7bdat.MissingHeader;
import sasquatch.biostatmatt.Sas7bdat.RowsInfo;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.AbstractList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sasquatch.biostatmatt.BiostatmattReader.readFrame;
import static sasquatch.biostatmatt.RUtils.DataType.CHARACTER;
import static sasquatch.biostatmatt.RUtils.DataType.NUMERIC;
import static sasquatch.samples.SasResources.*;

/**
 * @author Philippe Charles
 */
public class Sas7bdatTest {

    @Test
    public void test_32_little() throws IOException {
        RFrame frame = readFrame(LITTLE_32);

        assertThat(frame.getAttributes())
                .containsKey("column.info")
                .containsEntry("date.created", 9.858571072030001E8)
                .containsEntry("date.modified", 9.94683579329E8)
                .containsEntry("SAS.release", "8.0101M0")
                .containsEntry("SAS.host", "WIN_NT")
                .containsEntry("OS.version", "")
                .containsEntry("OS.maker", "WIN")
                .containsEntry("OS.name", "")
                .containsEntry("endian", ByteOrder.LITTLE_ENDIAN)
                .containsEntry("winunix", "windows")
                .containsEntry("missingHeader", new MissingHeader("STATESEX2", "DATA", 1, 4096, false, true))
                .containsEntry("rowsInfo", new RowsInfo(32, 50, 86));

        assertThat((RList<Column>) frame.getAttributes().get("column.info"))
                .containsExactly(
                        new Column("State", 16, 16, CHARACTER, null, null),
                        new Column("EnterDate", 0, 8, NUMERIC, "DATE", null),
                        new Column("Size", 8, 8, NUMERIC, null, null));

        assertThat(asList(frame.getData().get("State")))
                .hasSize(50)
                .contains("Delaware", atIndex(0))
                .contains("Hawaii", atIndex(49));

        assertThat(asList(frame.getData().get("EnterDate")))
                .hasSize(50)
                .contains(-62846d, atIndex(0))
                .contains(-133d, atIndex(49));

        assertThat(asList(frame.getData().get("Size")))
                .hasSize(50)
                .contains(1955d, atIndex(0))
                .contains(6423d, atIndex(49));
    }

    @Test
    public void test_32_big() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> readFrame(BIG_32));
    }

    @Test
    public void test_64_little() throws IOException {
        RFrame frame = readFrame(LITTLE_64);

        assertThat(frame.getAttributes())
                .containsKey("column.info")
                .containsEntry("date.created", 1.569533006860186E9)
                .containsEntry("date.modified", 1.569533006860186E9)
                .containsEntry("SAS.release", "9.0401M1")
                .containsEntry("SAS.host", "Linux")
                .containsEntry("OS.version", "2.6.32-754.9.1.e")
                .containsEntry("OS.maker", "")
                .containsEntry("OS.name", "x86_64")
                .containsEntry("endian", ByteOrder.LITTLE_ENDIAN)
                .containsEntry("winunix", "unix")
                .containsEntry("missingHeader", new MissingHeader("TEST7", "DATA", 1, 65536, true, true))
                .containsEntry("rowsInfo", new RowsInfo(816, 10, 62));

        assertThat(asList((RList<Column>) frame.getAttributes().get("column.info")))
                .hasSize(100)
                .contains(new Column("Column2", 600, 9, CHARACTER, "$", "Column 2 label"), atIndex(1))
                .contains(new Column("Column5", 24, 8, NUMERIC, "BEST", null), atIndex(4));

        assertThat(asList(frame.getData().get("Column2")))
                .hasSize(10)
                .contains("pear", atIndex(0))
                .contains("crocodile", atIndex(7));

        assertThat(asList(frame.getData().get("Column5")))
                .hasSize(10)
                .contains(0.103, atIndex(0))
                .contains(0.411, atIndex(7));
    }

    @Test
    public void test_64_big() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> readFrame(BIG_64));
    }

    static void debutAttributes(RFrame frame) {
        frame.getAttributes().forEach((k, v) -> System.out.println(k + " > " + v));
        for (Column o : (RList<Column>) frame.getAttributes().get("column.info")) {
            System.out.println(o);
        }
    }

    static <T> List<T> asList(final RUtils.RList<T> list) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return list.get(index + 1);
            }

            @Override
            public int size() {
                return RUtils.length(list);
            }
        };
    }

    static <T> List<T> asList(final RUtils.RVector<T> list) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return list.get(index + 1);
            }

            @Override
            public int size() {
                return RUtils.length(list);
            }
        };
    }
}
