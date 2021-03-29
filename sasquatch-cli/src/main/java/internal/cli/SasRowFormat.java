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

import sasquatch.SasColumn;
import sasquatch.SasRow;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class SasRowFormat {

    public static final SasRowFormat DEFAULT = SasRowFormat.builder().build();

    @lombok.NonNull
    Locale locale;

    @lombok.NonNull
    String datePattern;

    @lombok.NonNull
    String timePattern;

    @lombok.NonNull
    String dateTimePattern;

    @lombok.NonNull
    String numberPattern;

    boolean ignoreNumberGrouping;

    @lombok.NonNull
    String missingValue;

    public static Builder builder() {
        return new Builder()
                .locale(Locale.ROOT)
                .datePattern("yyyy-MM-dd")
                .timePattern("HH:mm:ss")
                .dateTimePattern("yyyy-MM-dd HH:mm:ss")
                .numberPattern("")
                .ignoreNumberGrouping(false)
                .missingValue("");
    }

    public DateTimeFormatter newDateFormatter() {
        return DateTimeFormatter.ofPattern(datePattern, locale);
    }

    public DateTimeFormatter newTimeFormatter() {
        return DateTimeFormatter.ofPattern(timePattern, locale);
    }

    public DateTimeFormatter newDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(dateTimePattern, locale);
    }

    public NumberFormat newNumberFormat() {
        DecimalFormat result = new DecimalFormat(numberPattern, new DecimalFormatSymbols(locale));
        if (ignoreNumberGrouping) {
            result.setGroupingUsed(false);
        }
        return result;
    }

    public SasRow.Mapper<String> asMapper(SasColumn column) {
        switch (column.getType()) {
            case CHARACTER: {
                return row -> {
                    String value = row.getString(column.getOrder());
                    return value != null ? value : getMissingValue();
                };
            }
            case NUMERIC: {
                NumberFormat f = newNumberFormat();
                return row -> {
                    double value = row.getNumber(column.getOrder());
                    return !Double.isNaN(value) ? f.format(value) : getMissingValue();
                };
            }
            case DATE: {
                DateTimeFormatter f = newDateFormatter();
                return row -> {
                    LocalDate value = row.getDate(column.getOrder());
                    return value != null ? f.format(value) : getMissingValue();
                };
            }
            case DATETIME: {
                DateTimeFormatter f = newDateTimeFormatter();
                return row -> {
                    LocalDateTime value = row.getDateTime(column.getOrder());
                    return value != null ? f.format(value) : getMissingValue();
                };
            }
            case TIME: {
                DateTimeFormatter f = newTimeFormatter();
                return row -> {
                    LocalTime value = row.getTime(column.getOrder());
                    return value != null ? f.format(value) : getMissingValue();
                };
            }
            default:
                return row -> {
                    Object value = row.getValue(column.getOrder());
                    return value != null ? value.toString() : getMissingValue();
                };
        }
    }

    public List<SasRow.Mapper<String>> asMappers(List<SasColumn> columns) {
        return columns
                .stream()
                .map(this::asMapper)
                .collect(Collectors.toList());
    }
}
