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

import nbbrd.console.picocli.LocaleConverter;
import picocli.CommandLine;

import java.util.Locale;

/**
 * @author Philippe Charles
 */
@lombok.Data
public final class RowFormatterOptions {

    @CommandLine.Option(
            names = {"-L", "--locale"},
            paramLabel = "<locale>",
            description = "Locale used to format dates, times and numbers.",
            converter = LocaleConverter.class,
            defaultValue = ""
    )
    private Locale locale;

    @CommandLine.Option(
            names = {"-D", "--date"},
            paramLabel = "<pattern>",
            description = "Pattern used to format dates.",
            defaultValue = "yyyy-MM-dd"
    )
    private String datePattern;

    @CommandLine.Option(
            names = {"-T", "--time"},
            paramLabel = "<pattern>",
            description = "Pattern used to format times.",
            defaultValue = "HH:mm:ss"
    )
    private String timePattern;

    @CommandLine.Option(
            names = {"-S", "--datetime"},
            paramLabel = "<pattern>",
            description = "Pattern used to format dates and times.",
            defaultValue = "yyyy-MM-dd HH:mm:ss"
    )
    private String dateTimePattern;

    @CommandLine.Option(
            names = {"-N", "--number"},
            paramLabel = "<pattern>",
            description = "Pattern used to format numbers.",
            defaultValue = ""
    )
    private String numberPattern;

    @CommandLine.Option(
            names = {"--no-grouping"},
            description = "Ignore number grouping.",
            defaultValue = "false"
    )
    private boolean ignoreNumberGrouping;

    @CommandLine.Option(
            names = {"-M", "--missing"},
            paramLabel = "<text>",
            description = "Text used to replace missing values.",
            defaultValue = ""
    )
    private String missingValue;

    public SasRowFormat toRowFormat() {
        return SasRowFormat
                .builder()
                .locale(locale)
                .datePattern(datePattern)
                .timePattern(timePattern)
                .dateTimePattern(dateTimePattern)
                .numberPattern(numberPattern)
                .ignoreNumberGrouping(ignoreNumberGrouping)
                .missingValue(missingValue)
                .build();
    }
}
