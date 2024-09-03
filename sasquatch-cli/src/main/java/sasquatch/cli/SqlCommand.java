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

import internal.cli.SasBasicSqlFormat;
import internal.cli.SasquatchCommand;
import nbbrd.console.picocli.MultiFileInputOptions;
import nbbrd.console.picocli.text.TextOutputOptions;
import nbbrd.io.text.TextFormatter;
import picocli.CommandLine;
import sasquatch.util.SasFilenameFilter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
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
public final class SqlCommand extends SasquatchCommand {

    @CommandLine.Mixin
    private MultiFileInputOptions input = new MultiFileInputOptions();

    @CommandLine.ArgGroup(validate = false, heading = "%nSQL options:%n")
    private TextOutputOptions output = new TextOutputOptions();

    @Override
    public Void call() throws Exception {
        TextFormatter<Path> formatter = getSqlFormatter();

        try (Writer charWriter = output.newCharWriter()) {
            if (input.isSingleFile()) {
                formatter.formatWriter(input.getSingleFile(), charWriter);
            } else {
                input.getAllFiles(new SasFilenameFilter()::accept)
                        .forEach(input.asConsumer(file -> formatter.formatWriter(file, charWriter), this::log));
            }
        }

        return null;
    }

    private TextFormatter<Path> getSqlFormatter() throws IOException {
        return SasBasicSqlFormat
                .builder()
                .sasquatch(getSasquatch())
                .build()
                .getFormatter();
    }

    private void log(Exception ex, Path file) {
        log.log(Level.INFO, "While reading '" + file + "'", ex);
    }
}
