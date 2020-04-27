/*
 * Copyright 2020 National Bank of Belgium
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
import internal.cli.YamlOptions;
import internal.ri.assumptions.SasFileAssumption;
import internal.ri.assumptions.SasFileError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "check"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugCheckCommand extends BaseCommand {

    @CommandLine.Mixin
    private MultiFileCommand files = new MultiFileCommand();
    
    @CommandLine.ArgGroup
    private YamlOptions yaml = new YamlOptions();

    @Override
    protected void exec() throws Exception {
        if (files.isSingleFile()) {
            yaml.dump(CheckReport.class, createReport(files.getSingleFile()));
        } else {
            List<CheckReport> items = files.getFiles()
                    .stream()
                    .parallel()
                    .map(files.asFunction(this::createReport))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            yaml.dumpAll(CheckReport.class, items);
        }
    }

    private CheckReport createReport(Path file) throws IOException {
        List<CheckReportItem> errors = new ArrayList<>();
        SasFileAssumption.testAll(file, error -> errors.add(CheckReportItem.of(error)));
        return new CheckReport(file.toString(), errors);
    }

    private boolean testReport(CheckReport report) {
        return !report.errors.isEmpty();
    }

    @lombok.Value
    public static class CheckReport {

        private String file;
        private List<CheckReportItem> errors;
    }

    @lombok.Value
    public static class CheckReportItem {

        public static CheckReportItem of(SasFileError error) {
            return new CheckReportItem(error.getAssumption().getName(), error.getDetails());
        }

        private String name;
        private String details;
    }
}
