package internal.samples;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import nbbrd.service.ServiceProvider;
import sasquatch.SasColumn;
import sasquatch.SasColumnFormat;
import sasquatch.SasRow;
import sasquatch.samples.CsvContent;
import sasquatch.samples.SasContent;
import sasquatch.samples.SasResources;

/**
 * Most of the code is copy-paste from
 * https://github.com/epam/parso/blob/master/src/main/java/com/epam/parso/DataWriterUtil.java
 * to avoid dependency hell
 */
@ServiceProvider(SasContent.class)
public final class EpamContent extends CsvContent {

    @Override
    public String getName() {
        return "Epam";
    }

    private final Path root = SasResources.EPAM.getRoot();

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
    protected SasRow.Mapper<String> getColumnFunc(SasColumn c) {
        int columnIndex = c.getOrder();
        switch (c.getType()) {
            case CHARACTER: {
                return row -> {
                    String result = row.getString(columnIndex);
                    return result == null || result.equals("null") ? "" : result;
                };
            }
            case NUMERIC: {
                DoubleFunction<String> formatter = c.getFormat().getName().equals("PERCENT")
                        ? new PercentFormatter(c.getFormat())
                        : DoubleFormatter.INSTANCE;
                return row -> formatter.apply(row.getNumber(columnIndex));
            }
            case DATE: {
                DateFormatter formatter = new DateFormatter(c.getFormat().getName());
                return row -> formatter.apply(row.getDate(columnIndex));
            }
            case DATETIME: {
                DateTimeFormatter formatter = new DateTimeFormatter(c.getFormat().getName());
                return row -> formatter.apply(row.getDateTime(columnIndex));
            }
            case TIME: {
                return row -> {
                    LocalTime time = row.getTime(columnIndex);
                    return TimeFormatter.INSTANCE.apply(time);
                };
            }
        }
        throw new RuntimeException("Unknown type");
    }

    private enum DoubleFormatter implements DoubleFunction<String> {
        INSTANCE;

        @Override
        public String apply(double value) {
            return Double.isNaN(value) ? "" : (isLong(value) ? longToString(value) : convertDoubleElementToString(value));
        }

        private static final double EPSILON = 1.0E-14;

        private static boolean isLong(double x) {
            return !(Math.abs(x - Math.round(x)) >= EPSILON);
        }

        private static String longToString(double x) {
            return Long.toString((long) x);
        }

        private static final int ACCURACY = 15;
        private static final int ROUNDING_LENGTH = 13;

        private static String convertDoubleElementToString(Double value) {
            String valueToPrint = String.valueOf(value);
            if (valueToPrint.length() > ROUNDING_LENGTH) {
                int lengthBeforeDot = (int) Math.ceil(Math.log10(Math.abs(value)));
                BigDecimal bigDecimal = new BigDecimal(value);
                bigDecimal = bigDecimal.setScale(ACCURACY - lengthBeforeDot, BigDecimal.ROUND_HALF_UP);
                valueToPrint = String.valueOf(bigDecimal.doubleValue());
            }
            valueToPrint = trimZerosFromEnd(valueToPrint);
            return valueToPrint;
        }

        private static String trimZerosFromEnd(String string) {
            return string.contains(".") ? string.replaceAll("0*$", "").replaceAll("\\.$", "") : string;
        }
    }

    private static class PercentFormatter implements DoubleFunction<String> {

        private final Format format;

        public PercentFormatter(SasColumnFormat columnFormat) {
            this.format = getPercentFormatProcessor(columnFormat, Locale.UK);
        }

        @Override
        public String apply(double t) {
            return !Double.isNaN(t) ? format.format(t) : "";
        }

        private static Format getPercentFormatProcessor(SasColumnFormat columnFormat, Locale locale) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
            if (columnFormat.getPrecision() == 0) {
                return new DecimalFormat("0%", dfs);
            }
            String pattern = "0%." + new String(new char[columnFormat.getPrecision()]).replace("\0", "0");
            return new DecimalFormat(pattern, dfs);
        }
    }

    private enum TimeFormatter implements Function<LocalTime, String> {
        INSTANCE;

        @Override
        public String apply(LocalTime t) {
            return t != null ? convertTimeElementToString(t.getLong(ChronoField.SECOND_OF_DAY)) : "";
        }

        private static final String HOURS_OUTPUT_FORMAT = "%02d";
        private static final String MINUTES_OUTPUT_FORMAT = "%02d";
        private static final String SECONDS_OUTPUT_FORMAT = "%02d";
        private static final String TIME_DELIMETER = ":";
        private static final int SECONDS_IN_MINUTE = 60;
        private static final int MINUTES_IN_HOUR = 60;

        private static String convertTimeElementToString(Long secondsFromMidnight) {

            return String.format(HOURS_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE / MINUTES_IN_HOUR)
                    + TIME_DELIMETER
                    + String.format(MINUTES_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE % MINUTES_IN_HOUR)
                    + TIME_DELIMETER
                    + String.format(SECONDS_OUTPUT_FORMAT, secondsFromMidnight % SECONDS_IN_MINUTE);

        }
    }

    private static class DateFormatter implements Function<LocalDate, String> {

        private final Format format;
        private final ZoneId zoneId;

        public DateFormatter(String columnFormat) {
            this.format = getDateFormatProcessor(columnFormat, Locale.UK);
            this.zoneId = ZoneId.of(Locale.UK.getCountry());
        }

        @Override
        public String apply(LocalDate t) {
            return t != null ? format.format(Date.from(t.atStartOfDay().atZone(zoneId).toInstant())) : "";
        }
    }

    private static class DateTimeFormatter implements Function<LocalDateTime, String> {

        private final Format format;
        private final ZoneId zoneId;

        public DateTimeFormatter(String columnFormat) {
            this.format = getDateFormatProcessor(columnFormat, Locale.UK);
            this.zoneId = ZoneId.of(Locale.UK.getCountry());
        }

        @Override
        public String apply(LocalDateTime t) {
            return t != null ? fixLowercaseUK(format.format(Date.from(t.atZone(zoneId).toInstant()))) : "";
        }

        //SimpleDateFormat outputs AM and PM as lower case
        //https://bugs.openjdk.java.net/browse/JDK-8211985
        private String fixLowercaseUK(String x) {
            return x.replace("am", "AM").replace("pm", "PM");
        }
    }

    private static Format getDateFormatProcessor(String columnFormat, Locale locale) {
        String pattern = DateTimeConstants.DATE_FORMAT_STRINGS.containsKey(columnFormat)
                ? DateTimeConstants.DATE_FORMAT_STRINGS.get(columnFormat)
                : DateTimeConstants.DATETIME_FORMAT_STRINGS.get(columnFormat);
        if (pattern == null) {
            throw new NoSuchElementException("UNKNOWN_DATE_FORMAT_EXCEPTION");

        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(Locale.UK.getCountry())));
        return dateFormat;
    }

    private interface DateTimeConstants {

        /**
         * These are sas7bdat format references to
         * {@link java.text.SimpleDateFormat} date formats.
         * <p>
         * UNSUPPORTED FORMATS: DTYYQC, PDJULG, PDJULI, QTR, QTRR, WEEKU, WEEKV,
         * WEEKW, YYQ, YYQC, YYQD, YYQN, YYQP, YYQS, YYQR, YYQRC, YYQRD, YYQRN,
         * YYQRP, YYQRS
         */
        Map<String, String> DATE_FORMAT_STRINGS = Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("B8601DA", "yyyyMMdd");
                put("E8601DA", "yyyy-MM-dd");
                put("DATE", "ddMMMyyyy");
                put("DAY", "dd");
                put("DDMMYY", "dd/MM/yyyy");
                put("DDMMYYB", "dd MM yyyy");
                put("DDMMYYC", "dd:MM:yyyy");
                put("DDMMYYD", "dd-MM-yyyy");
                put("DDMMYYN", "ddMMyyyy");
                put("DDMMYYP", "dd.MM.yyyy");
                put("DDMMYYS", "dd/MM/yyyy");
                put("JULDAY", "D");
                put("JULIAN", "yyyyD");
                put("MMDDYY", "MM/dd/yyyy");
                put("MMDDYYB", "MM dd yyyy");
                put("MMDDYYC", "MM:dd:yyyy");
                put("MMDDYYD", "MM-dd-yyyy");
                put("MMDDYYN", "MMddyyyy");
                put("MMDDYYP", "MM.dd.yyyy");
                put("MMDDYYS", "MM/dd/yyyy");
                put("MMYY", "MM'M'yyyy");
                put("MMYYC", "MM:yyyy");
                put("MMYYD", "MM-yyyy");
                put("MMYYN", "MMyyyy");
                put("MMYYP", "MM.yyyy");
                put("MMYYS", "MM/yyyy");
                put("MONNAME", "MMMM");
                put("MONTH", "M");
                put("MONYY", "MMMyyyy");
                put("WEEKDATE", "EEEE, MMMM dd, yyyy");
                put("WEEKDATX", "EEEE, dd MMMM, yyyy");
                put("WEEKDAY", "u");
                put("DOWNAME", "EEEE");
                put("WORDDATE", "MMMM d, yyyy");
                put("WORDDATX", "d MMMM yyyy");
                put("YYMM", "yyyy'M'MM");
                put("YYMMC", "yyyy:MM");
                put("YYMMD", "yyyy-MM");
                put("YYMMN", "yyyyMM");
                put("YYMMP", "yyyy.MM");
                put("YYMMS", "yyyy/MM");
                put("YYMMDD", "yyyy-MM-dd");
                put("YYMMDDB", "yyyy MM dd");
                put("YYMMDDC", "yyyy:MM:dd");
                put("YYMMDDD", "yyyy-MM-dd");
                put("YYMMDDN", "yyyyMMdd");
                put("YYMMDDP", "yyyy.MM.dd");
                put("YYMMDDS", "yyyy/MM/dd");
                put("YYMON", "yyyyMMM");
                put("YEAR", "yyyy");
            }
        });

        /**
         * These are sas7bdat format references to
         * {@link java.text.SimpleDateFormat} datetime formats.
         */
        Map<String, String> DATETIME_FORMAT_STRINGS = Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("B8601DN", "yyyyMMdd");
                put("B8601DT", "yyyyMMdd'T'HHmmssSSS");
                put("B8601DX", "yyyyMMdd'T'HHmmssZ");
                put("B8601DZ", "yyyyMMdd'T'HHmmssZ");
                put("B8601LX", "yyyyMMdd'T'HHmmssZ");
                put("E8601DN", "yyyy-MM-dd");
                put("E8601DT", "yyyy-MM-dd'T'HH:mm:ss.SSS");
                put("E8601DX", "yyyy-MM-dd'T'HH:mm:ssZ");
                put("E8601DZ", "yyyy-MM-dd'T'HH:mm:ssZ");
                put("E8601LX", "yyyy-MM-dd'T'HH:mm:ssZ");
                put("DATEAMPM", "ddMMMyyyy:HH:mm:ss.SS a");
                put("DATETIME", "ddMMMyyyy:HH:mm:ss.SS");
                put("DTDATE", "ddMMMyyyy");
                put("DTMONYY", "MMMyyyy");
                put("DTWKDATX", "EEEE, dd MMMM, yyyy");
                put("DTYEAR", "yyyy");
                put("MDYAMPM", "MM/dd/yyyy H:mm a");
                put("TOD", "HH:mm:ss.SS");
            }
        });

        /**
         * These are time formats that are used in sas7bdat files.
         */
        Set<String> TIME_FORMAT_STRINGS = new HashSet<>(Arrays.asList(
                "TIME", "HHMM", "E8601LZ", "E8601TM", "HOUR", "MMSS", "TIMEAMPM"
        ));
    }
}
