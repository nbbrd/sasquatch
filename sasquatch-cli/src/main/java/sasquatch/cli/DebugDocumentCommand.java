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
import internal.cli.MultiFileCommand;
import internal.cli.FilterOptions;
import internal.cli.YamlOptions;
import internal.ri.data.Document;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "doc"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugDocumentCommand extends BaseCommand {

    @CommandLine.Mixin
    private MultiFileCommand files = new MultiFileCommand();
    
    @CommandLine.ArgGroup(validate = false, heading = "%nFilter options:%n")
    private FilterOptions filter = new FilterOptions();

    @CommandLine.ArgGroup
    private YamlOptions yaml = new YamlOptions();

    @Override
    protected void exec() throws Exception {
        if (files.isSingleFile()) {
            yaml.dump(DocumentReport.class, createReport(files.getSingleFile()));
        } else {
            List<DocumentReport> items = files.getFiles()
                    .stream()
                    .parallel()
                    .map(files.asFunction(this::createReport))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            yaml.dumpAll(DocumentReport.class, items);
        }
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
