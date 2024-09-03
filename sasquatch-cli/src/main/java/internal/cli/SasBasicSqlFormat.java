package internal.cli;

import lombok.NonNull;
import nbbrd.io.text.TextFormatter;
import sasquatch.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

@lombok.Value
@lombok.Builder
public class SasBasicSqlFormat {

    @lombok.NonNull
    @lombok.Builder.Default
    Sasquatch sasquatch = Sasquatch.ofServiceLoader();

    public TextFormatter<Path> getFormatter() {
        return TextFormatter.onFormattingWriter(this::formatWriter);
    }

    private void formatWriter(@NonNull Path value, @NonNull Writer resource) throws IOException {
        SasRowFormat formats = getRowFormat();

        try (SasForwardCursor cursor = sasquatch.readForward(value)) {
            SasMetaData meta = cursor.getMetaData();

            // 1. Get table name and structure
            SqlWriter.Table table = getSqlTable(getTableName(meta.getName(), value), meta.getColumns());

            // 2. Create functions to get the values in a proper format
            SasRow.Mapper<String[]> rowMapper = getDumpRowMapper(formats.asMappers(meta.getColumns()));

            // 3. Retrieve data and write output
            SqlWriter output = new SqlWriter(resource);
            output.writeDropTableIfExist(table);
            output.writeCreateTable(table);
            while (cursor.next()) {
                output.writeInsertInto(table, rowMapper.apply(cursor));
            }
        }
    }

    private SasRowFormat getRowFormat() {
        return SasRowFormat.DEFAULT.toBuilder().ignoreNumberGrouping(true).build();
    }

    private static SasRow.Mapper<String[]> getDumpRowMapper(List<SasRow.Mapper<String>> mappers) {
        String[] values = new String[mappers.size()];
        return row -> {
            for (int i = 0; i < values.length; i++) {
                values[i] = mappers.get(i).apply(row);
            }
            return values;
        };
    }

    private static SqlWriter.Table getSqlTable(String name, List<SasColumn> columns) {
        SqlWriter.Table.Builder result = SqlWriter.Table.builder().name(name);
        SqlWriter.Column.Builder column = SqlWriter.Column.builder();
        columns.stream()
                .map(o -> column.name(o.getName()).type(toSqlColumnType(o)).build())
                .forEach(result::column);
        return result.build();
    }

    private static String toSqlColumnType(SasColumn column) {
        switch (column.getType()) {
            case CHARACTER:
                return "VARCHAR(" + column.getLength() + ")";
            case NUMERIC:
                return "DOUBLE";
            case DATE:
                return "DATE";
            case DATETIME:
                return "DATETIME";
            case TIME:
                return "TIME";
        }
        throw new RuntimeException();
    }

    private static String getTableName(String name, Path inputFile) {
        return name.isEmpty() ? inputFile.getFileName().toString() : name;
    }

    /**
     * @author Philippe Charles
     */
    @lombok.AllArgsConstructor
    private static final class SqlWriter {

        @lombok.Value
        @lombok.Builder
        public static class Table {

            String name;
            @lombok.Singular
            List<Column> columns;
        }

        @lombok.Value
        @lombok.Builder
        public static class Column {

            String name;
            String type;
        }

        @NonNull
        private final Writer writer;

        public void writeDropTableIfExist(Table table) throws IOException {
            writer.append("DROP TABLE IF EXISTS `").append(table.getName()).append("`;\n");
        }

        public void writeCreateTable(Table table) throws IOException {
            writer.append("CREATE TABLE `").append(table.getName()).append("` (");
            Iterator<Column> iterator = table.getColumns().iterator();
            if (iterator.hasNext()) {
                appendColumn(iterator.next());
                while (iterator.hasNext()) {
                    writer.append(", ");
                    appendColumn(iterator.next());
                }
            }
            writer.append(");\n");
        }

        public void writeInsertInto(Table table, String[] values) throws IOException {
            writer.append("INSERT INTO `").append(table.getName()).append("` VALUES(");
            for (int j = 0; j < values.length; j++) {
                writer.append("'").append(values[j]).append("'");
                if (j != values.length - 1) {
                    writer.append(", ");
                }
            }
            writer.append(");\n");
        }

        private void appendColumn(Column column) throws IOException {
            writer.append("`").append(column.getName()).append("` ").append(column.getType());
        }
    }
}
