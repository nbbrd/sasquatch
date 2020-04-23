/*
 * Copyright 2018 National Bank of Belgium
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
package internal.cli;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import sasquatch.SasColumn;
import sasquatch.SasRow;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor
public final class TextFormatter {

    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter dateTimeFormatter;
    private final DateTimeFormatter timeFormatter;
    private final NumberFormat numberFormat;
    private final String nullValue;

    public static TextFormatter of(Locale locale, String datePattern, String timePattern, String datetimePattern, String numberPattern, String nullValue) {
        return new TextFormatter(
                DateTimeFormatter.ofPattern(datePattern, locale),
                DateTimeFormatter.ofPattern(datetimePattern, locale),
                DateTimeFormatter.ofPattern(timePattern, locale),
                new DecimalFormat(numberPattern, new DecimalFormatSymbols(locale)),
                nullValue);
    }

    public SasRow.Mapper<String> asSasFunc(SasColumn column) {
        switch (column.getType()) {
            case CHARACTER:
                return o -> o.getString(column.getOrder());
            case NUMERIC: {
                NumberFormat f = getNumberFormat();
                return o -> {
                    double value = o.getNumber(column.getOrder());
                    return !Double.isNaN(value) ? f.format(value) : getNullValue();
                };
            }
            case DATE: {
                DateTimeFormatter f = getDateFormatter();
                return o -> {
                    LocalDate value = o.getDate(column.getOrder());
                    return value != null ? f.format(value) : getNullValue();
                };
            }
            case DATETIME: {
                DateTimeFormatter f = getDateTimeFormatter();
                return o -> {
                    LocalDateTime value = o.getDateTime(column.getOrder());
                    return value != null ? f.format(value) : getNullValue();
                };
            }
            case TIME: {
                DateTimeFormatter f = getTimeFormatter();
                return o -> {
                    LocalTime value = o.getTime(column.getOrder());
                    return value != null ? f.format(value) : getNullValue();
                };
            }
            default:
                return o -> {
                    Object value = o.getValue(column.getOrder());
                    return value != null ? value.toString() : getNullValue();
                };
        }
    }

    public SasRow.Mapper<String[]> asSasFuncs(List<SasColumn> columns) {
        return columns
                .stream()
                .map(this::asSasFunc)
                .collect(TextFormatter.grouping());
    }

    private static Collector<SasRow.Mapper<String>, ?, SasRow.Mapper<String[]>> grouping() {
        return Collectors.collectingAndThen(Collectors.toList(), TextFormatter::group);
    }

    private static SasRow.Mapper<String[]> group(List<SasRow.Mapper<String>> mappers) {
        final String[] row = new String[mappers.size()];
        return rs -> {
            for (int j = 0; j < row.length; j++) {
                row[j] = mappers.get(j).apply(rs);
            }
            return row;
        };
    }
}
