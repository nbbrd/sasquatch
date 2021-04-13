/*
 * Copyright 2016 National Bank of Belgium
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
package internal.picocli.sql;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class SqlWriter implements Closeable {

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

    @lombok.NonNull
    private final Writer writer;

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public SqlWriter dropTableIfExist(Table table) throws IOException {
        writer.append("DROP TABLE IF EXISTS `").append(table.getName()).append("`;\n");
        return this;
    }

    public SqlWriter createTable(Table table) throws IOException {
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
        return this;
    }

    public SqlWriter insertInto(Table table, String[] values) throws IOException {
        writer.append("INSERT INTO `").append(table.getName()).append("` VALUES(");
        for (int j = 0; j < values.length; j++) {
            writer.append("'").append(values[j]).append("'");
            if (j != values.length - 1) {
                writer.append(", ");
            }
        }
        writer.append(");\n");
        return this;
    }

    private void appendColumn(Column column) throws IOException {
        writer.append("`").append(column.getName()).append("` ").append(column.getType());
    }
}
