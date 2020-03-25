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
package internal.ri.base;

import internal.bytes.PValue;
import java.io.IOException;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static sasquatch.samples.SasResources.BIG_32;
import static sasquatch.samples.SasResources.BIG_64;
import static sasquatch.samples.SasResources.LITTLE_32;
import static sasquatch.samples.SasResources.LITTLE_64;

/**
 *
 * @author Philippe Charles
 */
public class HeaderTest {

    @Test
    public void test_32_little() throws IOException {
        assertThat(Header.parse(LITTLE_32)).isEqualTo(new Header(
                false,
                ByteOrder.LITTLE_ENDIAN,
                PValue.known(Platform.WINDOWS),
                PValue.known(Encoding.DEFAULT),
                LocalDateTime.parse("2001-03-29T09:11:47.203"),
                LocalDateTime.parse("2001-07-09T12:59:39.329"),
                1024,
                4096,
                1,
                "8.0101M0",
                "WIN_NT",
                "",
                "WIN",
                "",
                "STATESEX2",
                "DATA"));
    }

    @Test
    public void test_32_big() throws IOException {
        assertThat(Header.parse(BIG_32)).isEqualTo(new Header(
                false,
                ByteOrder.BIG_ENDIAN,
                PValue.known(Platform.UNIX),
                LATIN1,
                LocalDateTime.parse("2019-09-26T21:23:26.957"),
                LocalDateTime.parse("2019-09-26T21:23:26.957"),
                65536,
                65536,
                1,
                "9.0401M1",
                "Linux",
                "2.6.32-754.9.1.e",
                "",
                "x86_64",
                "TEST10",
                "DATA"));
    }

    @Test
    public void test_64_little() throws IOException {
        assertThat(Header.parse(LITTLE_64)).isEqualTo(new Header(
                true,
                ByteOrder.LITTLE_ENDIAN,
                PValue.known(Platform.UNIX),
                LATIN1,
                LocalDateTime.parse("2019-09-26T21:23:26.860"),
                LocalDateTime.parse("2019-09-26T21:23:26.860"),
                65536,
                65536,
                1,
                "9.0401M1",
                "Linux",
                "2.6.32-754.9.1.e",
                "",
                "x86_64",
                "TEST7",
                "DATA"));
    }

    @Test
    public void test_64_big() throws IOException {
        assertThat(Header.parse(BIG_64)).isEqualTo(new Header(
                true,
                ByteOrder.BIG_ENDIAN,
                PValue.known(Platform.UNIX),
                LATIN1,
                LocalDateTime.parse("2019-09-26T21:23:27.066"),
                LocalDateTime.parse("2019-09-26T21:23:27.066"),
                65536,
                65536,
                1,
                "9.0401M1",
                "Linux",
                "2.6.32-754.9.1.e",
                "",
                "x86_64",
                "TEST13",
                "DATA"));
    }

    private static final PValue<Encoding, Short> LATIN1 = Encoding.tryParse((short) 0x1D);
}
