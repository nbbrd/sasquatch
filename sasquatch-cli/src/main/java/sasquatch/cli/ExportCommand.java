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

import internal.cli.SasReaderCommand;
import internal.cli.TextFormatter;
import internal.cli.TextFormatterOptions;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import picocli.ext.CsvOptions;
import sasquatch.SasColumn;
import sasquatch.SasMetaData;
import sasquatch.SasResultSet;
import sasquatch.SasRowMapper;
import sasquatch.Sasquatch;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "export",
        description = "Export SAS7BDAT file(s) to a delimited text file such as CSV"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class ExportCommand extends SasReaderCommand {

    @CommandLine.Option(
            names = {"-t", "--data-type"},
            paramLabel = "<data_type>",
            description = "Type of data to export (HEADER, COLUMNS or ROWS)"
    )
    private DataType dataType = DataType.ROWS;

    @CommandLine.ArgGroup(validate = false, heading = "%nCSV options:%n")
    private CsvOptions.Output csv = new CsvOptions.Output();

    @CommandLine.ArgGroup(validate = false, heading = "%nText format:%n")
    private TextFormatterOptions formatter = new TextFormatterOptions();

    @Override
    protected void exec() throws Exception {
        if (dataType.equals(DataType.ROWS) && !isSingleFile()) {
            throw new IllegalArgumentException("Cannot export rows on multiple files");
        }

        try (Csv.Writer writer = csv.newWriter(this::getStdOutEncoding)) {
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
    }

    private void exportHeader(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        TextFormatter textFormatter = formatter.getFormatter();

        DateTimeFormatter dateTimeFormatter = textFormatter.getDateTimeFormatter();
        NumberFormat numberFormatter = textFormatter.getNumberFormat();

        if (isSingleFile()) {
            writeHeaderHead(writer);
            writeHeaderBody(sas.readMetaData(getSingleFile()), writer, dateTimeFormatter, numberFormatter);
        } else {
            writer.writeField("File");
            writeHeaderHead(writer);
            getFiles().forEach(asConsumer(file -> {
                writer.writeField(file.toString());
                writeHeaderBody(sas.readMetaData(file), writer, dateTimeFormatter, numberFormatter);
            }));
        }
    }

    private void writeHeaderHead(Csv.Writer output) throws IOException {
        output.writeField("Name");
        output.writeField("Created");
        output.writeField("Modified");
        output.writeField("Release");
        output.writeField("Host");
        output.writeField("Rows");
        output.writeEndOfLine();
    }

    private void writeHeaderBody(SasMetaData meta, Csv.Writer output, DateTimeFormatter dateTimeFormatter, NumberFormat numberFormatter) throws IOException {
        output.writeField(meta.getName());
        output.writeField(dateTimeFormatter.format(meta.getCreationTime()));
        output.writeField(dateTimeFormatter.format(meta.getLastModificationTime()));
        output.writeField(meta.getRelease());
        output.writeField(meta.getHost());
        output.writeField(numberFormatter.format(meta.getRowCount()));
        output.writeEndOfLine();
    }

    private void exportColumns(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        TextFormatter textFormatter = formatter.getFormatter();

        NumberFormat numberFormatter = textFormatter.getNumberFormat();

        if (isSingleFile()) {
            writeColumnsHead(writer);
            writeColumnsBody(sas.readMetaData(getSingleFile()), writer, numberFormatter, null);
        } else {
            writer.writeField("File");
            writeColumnsHead(writer);
            getFiles().forEach(asConsumer(file -> writeColumnsBody(sas.readMetaData(file), writer, numberFormatter, file.toString())));
        }
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

    private void writeColumnsBody(SasMetaData meta, Csv.Writer output, NumberFormat numberFormatter, String fileName) throws IOException {
        for (SasColumn o : meta.getColumns()) {
            if (fileName != null) {
                output.writeField(fileName);
            }
            output.writeField(numberFormatter.format(o.getOrder()));
            output.writeField(o.getName());
            output.writeField(o.getType().name());
            output.writeField(numberFormatter.format(o.getLength()));
            output.writeField(o.getFormat());
            output.writeField(o.getLabel());
            output.writeEndOfLine();
        }
    }

    private void exportRows(Csv.Writer writer) throws IOException {
        Sasquatch sas = getSasquatch();
        TextFormatter textFormatter = formatter.getFormatter();

        try (SasResultSet rs = sas.read(getSingleFile())) {
            List<SasColumn> columns = rs.getMetaData().getColumns();

            List<SasRowMapper<String>> fieldFunctions = new ArrayList<>();
            for (SasColumn o : columns) {
                writer.writeField(o.getName());
                fieldFunctions.add(textFormatter.asSasFunc(o));
            }
            writer.writeEndOfLine();

            while (rs.nextRow()) {
                for (SasRowMapper<String> o : fieldFunctions) {
                    writer.writeField(o.apply(rs));
                }
                writer.writeEndOfLine();
            }
        }
    }

    private enum DataType {

        HEADER, COLUMNS, ROWS;
    }
}
