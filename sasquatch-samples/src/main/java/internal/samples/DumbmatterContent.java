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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.DoubleFunction;
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
public final class DumbmatterContent extends CsvContent {

    private final Path root = SasResources.DUMBMATTER.getRoot();

    @Override
    protected Stream<Path> getSasFiles() {
        return SasResources.walk(root);
    }

    @Override
    protected Path resolveCsvFile(Path sasFile) {
        return sasFile
                .getParent()
                .getParent()
                .resolve("csv")
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
                return row -> StatTransfer13NumberFormat.INSTANCE.apply(row.getNumber(columnIndex));
            case DATE:
                return row -> {
                    LocalDate date = row.getDate(columnIndex);
                    return date != null ? date.format(DATE_FORMATTER) : "";
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    enum StatTransfer13NumberFormat implements DoubleFunction<String> {
        INSTANCE;

        private final DecimalFormat f1;
        private final DecimalFormat f2;

        private StatTransfer13NumberFormat() {
            this.f1 = new DecimalFormat();
            f1.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            f1.setMaximumFractionDigits(15);
            f1.setGroupingUsed(false);
            this.f2 = new DecimalFormat("0.0E00");
            DecimalFormatSymbols f2Symbols = new DecimalFormatSymbols(Locale.ROOT);
            f2Symbols.setExponentSeparator("e");
            f2.setDecimalFormatSymbols(f2Symbols);
            f2.setMaximumFractionDigits(15);
        }

        @Override
        public String apply(double value) {
            if (Double.isNaN(value)) {
                return "";
            }
            return Math.abs(value) < .0001 && value != 0
                    ? apply(f2, value)
                    : apply(f1, value);
        }

        static String apply(DecimalFormat f, double value) {
            f.setMaximumFractionDigits(16);
            String tmp = f.format(value);
            int shift = getDigitShift(tmp);
            f.setMaximumFractionDigits(15 - shift);
            return f.format(value);
        }

        static int getDigitShift(String tmp) {
            boolean before = true;
            boolean nonZero = false;
            int cpt = 0;
            for (char o : tmp.toCharArray()) {
                switch (o) {
                    case '-':
                        break;
                    case '.':
                        if (nonZero) {
                            return cpt;
                        }
                        before = false;
                        break;
                    case '0':
                        if (before) {
                            if (nonZero) {
                                cpt++;
                            }
                        } else {
                            cpt--;
                        }
                        break;
                    default:
                        if (before) {
                            nonZero = true;
                            cpt++;
                        } else {
                            return cpt;
                        }
                        break;
                }
            }
            return cpt;
        }
    }
}
