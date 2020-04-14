/*
 * Copyright 2017 National Bank of Belgium
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
package internal.samples;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;
import nbbrd.service.ServiceProvider;
import sasquatch.SasColumn;
import sasquatch.SasRowMapper;
import sasquatch.samples.CsvContent;
import sasquatch.samples.SasContent;
import sasquatch.samples.SasResources;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SasContent.class)
public final class KsheddenContent extends CsvContent {

    @Override
    public String getName() {
        return "Kshedden";
    }

    private final Path root = SasResources.KSHEDDEN.getRoot();

    @Override
    protected Stream<Path> getSasFiles() {
        return SasResources.walk(root);
    }

    @Override
    protected Path resolveCsvFile(Path sasFile) {
        return sasFile
                .getParent()
                .getParent()
                .resolve("ref")
                .resolve(sasFile.getFileName().toString().replace(".sas7bdat", ".csv"));
    }

    @Override
    protected Path relativizeSasFile(Path sasFile) {
        return root.relativize(sasFile);
    }

    @Override
    protected SasRowMapper<String> getColumnFunc(SasColumn c) {
        int columnIndex = c.getOrder();
        switch (c.getType()) {
            case CHARACTER:
                return row -> {
                    String result = row.getString(columnIndex);
                    return result == null || result.equals("null") ? "" : result;
                };
            case NUMERIC:
                return row -> {
                    double value = row.getNumber(columnIndex);
                    return Double.isNaN(value) ? "" : NUMBER_FORMATTER.format(value);
                };
            case DATE:
                return row -> {
                    LocalDate date = row.getDate(columnIndex);
                    return date != null ? (date.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00:00 +0000 UTC") : "";
                };
            case DATETIME:
                return row -> {
                    LocalDateTime dateTime = row.getDateTime(columnIndex);
                    return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
                };
            case TIME:
                return row -> {
                    LocalTime time = row.getTime(columnIndex);
                    return time != null ? time.format(TIME_FORMATTER) : "";
                };
        }
        throw new RuntimeException("Unknown type");
    }

    private static final NumberFormat NUMBER_FORMATTER = new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.ROOT));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

}
