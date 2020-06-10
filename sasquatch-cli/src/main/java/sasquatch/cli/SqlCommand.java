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
import internal.cli.SasRowFormat;
import internal.picocli.sql.SqlOutputOptions;
import internal.picocli.sql.SqlWriter;
import nbbrd.console.picocli.MultiFileInputOptions;
import picocli.CommandLine;
import sasquatch.*;
import sasquatch.util.SasFilenameFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "sql",
        description = "Dump SAS dataset to SQL script."
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class SqlCommand extends SasReaderCommand {

    @CommandLine.Mixin
    private MultiFileInputOptions input = new MultiFileInputOptions();

    @CommandLine.ArgGroup(validate = false, heading = "%nSQL options:%n")
    private SqlOutputOptions output = new SqlOutputOptions();

    @Override
    public Void call() throws Exception {
        try (SqlWriter sql = output.newSqlWriter(this::getStdOutEncoding)) {
            Sasquatch sas = getSasquatch();
            if (input.isSingleFile()) {
                dump(sas, input.getSingleFile(), sql, SasRowFormat.DEFAULT);
            } else {
                input.getAllFiles(new SasFilenameFilter()::accept)
                        .forEach(input.asConsumer(file -> dump(sas, file, sql, SasRowFormat.DEFAULT), this::log));
            }
        }
        return null;
    }

    private void log(Exception ex, Path file) {
        log.log(Level.INFO, "While reading '" + file + "'", ex);
    }

    private static void dump(Sasquatch reader, Path input, SqlWriter output, SasRowFormat formats) throws IOException {
        try (SasForwardCursor cursor = reader.readForward(input)) {
            SasMetaData meta = cursor.getMetaData();

            // 1. Get table name and structure
            SqlWriter.Table table = getSqlTable(getTableName(meta.getName(), input), meta.getColumns());

            // 2. Create functions to get the values in a proper format
            SasRow.Mapper<String[]> rowMapper = getDumpRowMapper(formats.asMappers(meta.getColumns()));

            // 3. Retrieve data and write output
            output.dropTableIfExist(table).createTable(table);
            while (cursor.next()) {
                output.insertInto(table, rowMapper.apply(cursor));
            }
        }
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
}
