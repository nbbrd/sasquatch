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
import internal.cli.SasReaderCommand;
import internal.cli.SasRowFormat;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sasquatch.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "csv",
        description = "Dump SAS dataset to CSV file."
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class CsvCommand extends SasReaderCommand {

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
        try (Csv.Writer writer = output.newCsvWriter(this::getStdOutEncoding)) {
            switch (dataType) {
                case HEADER:
                    exportHeader(writer);
                    break;
                case COLUMNS:
                    exportColumns(writer);
                    break;
                case ROWS:
                    exportRows(writer);
                    break;
            }
        }
        return null;
    }

    private void exportHeader(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        SasRowFormat rowFormat = formatter.toRowFormat();

        DateTimeFormatter dateTimeFormatter = rowFormat.newDateTimeFormatter();
        NumberFormat numberFormatter = rowFormat.newNumberFormat();

        writeHeaderHead(writer);
        writeHeaderBody(sas.readMetaData(input), writer, dateTimeFormatter, numberFormatter);
    }

    private void writeHeaderHead(Csv.Writer output) throws IOException {
        output.writeField("Name");
        output.writeField("Label");
        output.writeField("Created");
        output.writeField("Modified");
        output.writeField("Release");
        output.writeField("Host");
        output.writeField("Rows");
        output.writeEndOfLine();
    }

    private void writeHeaderBody(SasMetaData meta, Csv.Writer output, DateTimeFormatter dateTimeFormatter, NumberFormat numberFormatter) throws IOException {
        output.writeField(meta.getName());
        output.writeField(meta.getLabel());
        output.writeField(dateTimeFormatter.format(meta.getCreationTime()));
        output.writeField(dateTimeFormatter.format(meta.getLastModificationTime()));
        output.writeField(meta.getRelease());
        output.writeField(meta.getHost());
        output.writeField(numberFormatter.format(meta.getRowCount()));
        output.writeEndOfLine();
    }

    private void exportColumns(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        SasRowFormat rowFormat = formatter.toRowFormat();

        NumberFormat numberFormatter = rowFormat.newNumberFormat();

        writeColumnsHead(writer);
        writeColumnsBody(sas.readMetaData(input), writer, numberFormatter);
    }

    private void writeColumnsHead(Csv.Writer output) throws IOException {
        output.writeField("Order");
        output.writeField("Name");
        output.writeField("Type");
        output.writeField("Length");
        output.writeField("Format");
        output.writeField("Label");
        output.writeEndOfLine();
    }

    private void writeColumnsBody(SasMetaData meta, Csv.Writer output, NumberFormat numberFormatter) throws IOException {
        for (SasColumn o : meta.getColumns()) {
            output.writeField(numberFormatter.format(o.getOrder()));
            output.writeField(o.getName());
            output.writeField(o.getType().name());
            output.writeField(numberFormatter.format(o.getLength()));
            output.writeField(o.getFormat().toString());
            output.writeField(o.getLabel());
            output.writeEndOfLine();
        }
    }

    private void exportRows(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        SasRowFormat rowFormat = formatter.toRowFormat();

        try (SasForwardCursor cursor = sas.readForward(input)) {
            List<SasRow.Mapper<String>> mappers = new ArrayList<>();
            for (SasColumn o : cursor.getColumns()) {
                writer.writeField(o.getName());
                mappers.add(rowFormat.asMapper(o));
            }
            writer.writeEndOfLine();

            while (cursor.next()) {
                for (SasRow.Mapper<String> o : mappers) {
                    writer.writeField(o.apply(cursor));
                }
                writer.writeEndOfLine();
            }
        }
    }

    private enum DataType {

        HEADER, COLUMNS, ROWS;
    }
}
