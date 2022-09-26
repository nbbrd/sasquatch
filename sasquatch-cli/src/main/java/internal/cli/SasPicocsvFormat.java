package internal.cli;

import lombok.NonNull;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;
import sasquatch.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@lombok.Value
@lombok.Builder
public class SasPicocsvFormat {

    public enum DataType {

        HEADER, COLUMNS, ROWS;
    }

    @NonNull
    @lombok.Builder.Default
    Sasquatch sasquatch = Sasquatch.ofServiceLoader();

    @NonNull
    @lombok.Builder.Default
    SasRowFormat rowFormat = SasRowFormat.DEFAULT;

    @NonNull
    @lombok.Builder.Default
    DataType dataType = DataType.ROWS;

    public Picocsv.Formatter<Path> getFormatter() {
        return Picocsv.Formatter.builder(this::format).build();
    }

    private void format(@NonNull Path value, @NonNull Csv.Writer writer) throws IOException {
        switch (dataType) {
            case HEADER:
                exportHeader(value, writer);
                break;
            case COLUMNS:
                exportColumns(value, writer);
                break;
            case ROWS:
                exportRows(value, writer);
                break;
        }
    }

    private void exportHeader(Path input, Csv.Writer writer) throws IOException {
        DateTimeFormatter dateTimeFormatter = rowFormat.newDateTimeFormatter();
        NumberFormat numberFormatter = rowFormat.newNumberFormat();

        writeHeaderHead(writer);
        writeHeaderBody(sasquatch.readMetaData(input), writer, dateTimeFormatter, numberFormatter);
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

    private void exportColumns(Path input, Csv.Writer writer) throws IOException {
        NumberFormat numberFormatter = rowFormat.newNumberFormat();

        writeColumnsHead(writer);
        writeColumnsBody(sasquatch.readMetaData(input), writer, numberFormatter);
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

    private void exportRows(Path input, Csv.Writer writer) throws IOException {
        try (SasForwardCursor cursor = sasquatch.readForward(input)) {
            List<SasRow.Mapper<String>> mappers = new ArrayList<>();

            for (SasColumn o : cursor.getColumns()) {
                writer.writeField(o.getName());
                mappers.add(rowFormat.asMapper(o));
            }
            writer.writeEndOfLine();

            writeRowsBody(writer, cursor, mappers);
        }
    }

    private void writeRowsBody(Csv.Writer writer, SasForwardCursor cursor, List<SasRow.Mapper<String>> mappers) throws IOException {
        while (cursor.next()) {
            for (SasRow.Mapper<String> o : mappers) {
                writer.writeField(o.apply(cursor));
            }
            writer.writeEndOfLine();
        }
    }
}
