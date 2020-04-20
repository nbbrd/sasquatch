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
package _test;

import sasquatch.util.SasArray;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import org.assertj.core.util.Files;
import sasquatch.SasColumn;
import sasquatch.SasColumnType;
import sasquatch.SasMetaData;
import sasquatch.SasRow;
import sasquatch.Sasquatch;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Sample {

    public final LocalDateTime DATE = LocalDateTime.of(2010, 2, 3, 4, 5);
    public final Path FILE = Files.newTemporaryFile().toPath();

    public final Record ROW1 = new Record(3.14, "abc", DATE.toLocalDate(), DATE, DATE.toLocalTime());

    public final SasArray VALID_TABLE = SasArray
            .of(SasMetaData
                    .builder()
                    .column(SasColumn.builder().name("c1").order(0).type(SasColumnType.NUMERIC).build())
                    .column(SasColumn.builder().name("c2").order(1).type(SasColumnType.CHARACTER).build())
                    .column(SasColumn.builder().name("c3").order(2).type(SasColumnType.DATE).build())
                    .column(SasColumn.builder().name("c4").order(3).type(SasColumnType.DATETIME).build())
                    .column(SasColumn.builder().name("c5").order(4).type(SasColumnType.TIME).build())
                    .build(),
                    Collections.singletonList(ROW1.toArray()));

    public final FakeSasReader VALID_READER = FakeSasReader
            .builder()
            .name("valid")
            .available(true)
            .cost(SasReader.NATIVE)
            .features(EnumSet.allOf(SasFeature.class))
            .table(FILE, VALID_TABLE)
            .build();

    @lombok.Value
    public static class Record {

        private double c1;
        private String c2;
        private LocalDate c3;
        private LocalDateTime c4;
        private LocalTime c5;

        public Object[] toArray() {
            return new Object[]{c1, c2, c3, c4, c5};
        }
    }

    public static Record parseRecord(SasRow row) throws IOException {
        return new Record(row.getNumber(0), row.getString(1), row.getDate(2), row.getDateTime(3), row.getTime(4));
    }

    public static Path getSasTestFiles() {
        try {
            return Paths.get(Sasquatch.class.getResource("/").toURI())
                    .getParent()
                    .getParent()
                    .getParent()
                    .resolve("resources");
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
