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
package sasquatch.cli;

import internal.cli.RowFormatterOptions;
import internal.cli.SasPicocsvFormat;
import internal.cli.SasquatchCommand;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static internal.cli.SasPicocsvFormat.DataType;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "csv",
        description = "Dump SAS dataset to CSV file."
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class CsvCommand extends SasquatchCommand {

    @CommandLine.Parameters(
            paramLabel = "<file>",
            description = "Input file."
    )
    private Path input;

    @CommandLine.Option(
            names = {"-t", "--data-type"},
            paramLabel = "<data_type>",
            description = "Type of data to export (${COMPLETION-CANDIDATES})",
            defaultValue = "ROWS"
    )
    private DataType dataType;

    @CommandLine.ArgGroup(validate = false, heading = "%nCSV options:%n")
    private CsvOutputOptions output = new CsvOutputOptions();

    @CommandLine.ArgGroup(validate = false, heading = "%nRow format:%n")
    private RowFormatterOptions formatter = new RowFormatterOptions();

    @Override
    public Void call() throws Exception {
        Picocsv.Formatter<Path> csv = getCsvFormatter();

        try (Csv.Writer writer = output.newCsvWriter()) {
            csv.formatCsv(input, writer);
        }

        return null;
    }

    private Picocsv.Formatter<Path> getCsvFormatter() throws IOException {
        return SasPicocsvFormat
                .builder()
                .sasquatch(getSasquatch())
                .rowFormat(formatter.toRowFormat())
                .dataType(dataType)
                .build()
                .getFormatter();
    }
}
