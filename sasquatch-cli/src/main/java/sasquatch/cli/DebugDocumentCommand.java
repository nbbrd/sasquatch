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

import internal.cli.BaseCommand;
import internal.cli.DebugFilterOptions;
import internal.cli.DebugOutputOptions;
import internal.ri.data.Document;
import nbbrd.console.picocli.MultiFileInputOptions;
import picocli.CommandLine;
import sasquatch.util.SasFilenameFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "doc"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugDocumentCommand extends BaseCommand {

    @CommandLine.Mixin
    private MultiFileInputOptions input = new MultiFileInputOptions();

    @CommandLine.ArgGroup(validate = false, heading = "%nFilter options:%n")
    private DebugFilterOptions filter = new DebugFilterOptions();

    @CommandLine.ArgGroup
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    protected void exec() throws Exception {
        if (input.isSingleFile()) {
            output.dump(DocumentReport.class, createReport(input.getSingleFile()), this::getStdOutEncoding);
        } else {
            List<DocumentReport> items = input.getAllFiles(new SasFilenameFilter()::accept)
                    .stream()
                    .parallel()
                    .map(input.asFunction(this::createReport, this::log))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            output.dumpAll(DocumentReport.class, items, this::getStdOutEncoding);
        }
    }

    private void log(Exception ex, Path file) {
        log.log(Level.INFO, "While reading '" + file + "'", ex);
    }

    private DocumentReport createReport(Path file) throws IOException {
        return new DocumentReport(file.toString(), Document.parse(file));
    }

    private boolean testReport(DocumentReport item) {
        return filter.testScope(item.getDocument())
                && filter.testKeyValues(item.getDocument());
    }

    @lombok.Value
    public static final class DocumentReport {

        private String file;
        private Document document;
    }
}
