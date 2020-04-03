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

import internal.cli.SqlWriter;
import internal.cli.TextFormatter;
import internal.cli.SasReaderCommand;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import picocli.CommandLine;
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
        name = "sql",
        description = "Dump dataset to SQL script."
)
@SuppressWarnings("FieldMayBeFinal")
public final class SqlCommand extends SasReaderCommand {

    @CommandLine.Option(
            names = {"-o", "--output-file"},
            paramLabel = "<file>",
            description = "Output SQL file"
    )
    private Path output = null;

    @CommandLine.Option(
            names = {"-e", "--encoding"},
            paramLabel = "<encoding>",
            description = "Charset used to encode text."
    )
    private Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void exec() throws Exception {
        Sasquatch sas = getSasquatch();

        try (SqlWriter sql = newWriter()) {
            if (isSingleFile()) {
                dump(sas, getSingleFile(), sql, SQL_FORMAT);
            } else {
                getFiles().forEach(asConsumer(file -> dump(sas, file, sql, SQL_FORMAT)));
            }
        }
    }

    private SqlWriter newWriter() throws IOException {
        return output != null
                ? new SqlWriter(Files.newBufferedWriter(output, charset))
                : new SqlWriter(new PrintWriter(System.out));
    }

    private static TextFormatter SQL_FORMAT = TextFormatter.of(Locale.ROOT, "yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "", "");

    private static void dump(Sasquatch reader, Path input, SqlWriter output, TextFormatter formats) throws IOException {
        try (SasResultSet rs = reader.read(input)) {
            SasMetaData meta = rs.getMetaData();

            // 1. Get table name and structure
            SqlWriter.Table table = getSqlTable(getTableName(meta.getName(), input), meta.getColumns());

            // 2. Create functions to get the values in a proper format
            SasRowMapper<String[]> rowMapper = formats.asSasFuncs(meta.getColumns());

            // 3. Retrieve data and write output
            output.dropTableIfExist(table).createTable(table);
            while (rs.nextRow()) {
                output.insertInto(table, rowMapper.apply(rs));
            }
        }
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
