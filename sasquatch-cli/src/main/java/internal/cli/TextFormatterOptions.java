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

import java.util.Locale;
import picocli.CommandLine;
import picocli.ext.LocaleConverter;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public final class TextFormatterOptions {

    @CommandLine.Option(
            names = {"-L", "--locale"},
            paramLabel = "<locale>",
            description = "Locale used to format dates and numbers",
            converter = LocaleConverter.class
    )
    private Locale locale = Locale.ROOT;

    @CommandLine.Option(
            names = {"-D", "--date-pattern"},
            paramLabel = "<pattern>",
            description = "Pattern used to format dates"
    )
    private String datePattern = "yyyy-MM-dd";

    @CommandLine.Option(
            names = {"-T", "--time-pattern"},
            paramLabel = "<pattern>",
            description = "Pattern used to format times"
    )
    private String timePattern = "HH:mm:ss";

    @CommandLine.Option(
            names = {"-S", "--datetime-pattern"},
            paramLabel = "<pattern>",
            description = "Pattern used to format datetimes"
    )
    private String datetimePattern = "yyyy-MM-dd HH:mm:ss";

    @CommandLine.Option(
            names = {"-N", "--number-pattern"},
            paramLabel = "<pattern>",
            description = "Pattern used to format numbers"
    )
    private String numberPattern = "";

    @CommandLine.Option(
            names = {"-X", "--null-value"},
            paramLabel = "<value>",
            description = "Text used to replace null values"
    )
    private String nullValue = "";

    public TextFormatter getFormatter() {
        return TextFormatter.of(locale, datePattern, timePattern, datetimePattern, numberPattern, nullValue);
    }
}
