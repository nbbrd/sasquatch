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

import internal.cli.FileCommand;
import internal.cli.FilterOptions;
import internal.ri.base.Header;
import internal.cli.YamlOptions;
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
        name = "head"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugHeaderCommand extends FileCommand {

    @CommandLine.ArgGroup(validate = false, heading = "%nFilter options:%n")
    private FilterOptions filter = new FilterOptions();

    @CommandLine.ArgGroup
    private YamlOptions yaml = new YamlOptions();

    @Override
    protected void exec() throws Exception {
        if (isSingleFile()) {
            yaml.dump(HeaderReport.class, createReport(getSingleFile()));
        } else {
            List<HeaderReport> items = getFiles()
                    .stream()
                    .parallel()
                    .map(asFunction(this::createReport))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            yaml.dumpAll(HeaderReport.class, items);
        }
    }

    private HeaderReport createReport(Path file) throws IOException {
        return new HeaderReport(file.toString(), Header.parse(file));
    }

    private boolean testReport(HeaderReport item) {
        return filter.testScope(item.getHeader())
                && filter.testKeyValues(item.getHeader());
    }

    @lombok.Value
    public static final class HeaderReport {

        private String file;
        private Header header;
    }
}
