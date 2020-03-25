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

import internal.bytes.BytesReader;
import internal.cli.FileCommand;
import internal.cli.FilterOptions;
import internal.ri.base.Header;
import internal.cli.YamlOptions;
import internal.ri.base.PageHeader;
import internal.ri.base.PageType;
import internal.ri.base.SasFile;
import internal.ri.base.SasFileVisitor;
import internal.ri.base.SubHeaderFormat;
import internal.ri.base.SubHeaderPointer;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "file"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugFileCommand extends FileCommand {

    @CommandLine.ArgGroup(validate = false, heading = "%nFilter options:%n")
    private FilterOptions filter = new FilterOptions();

    @CommandLine.ArgGroup
    private YamlOptions yaml = new YamlOptions();

    @CommandLine.Option(
            names = {"-T", "--skip-type"},
            paramLabel = "<type>",
            description = "Skip page type (${COMPLETION-CANDIDATES}).",
            split = ","
    )
    private List<PageType> skipTypes = new ArrayList<>();

    @CommandLine.Option(
            names = {"-F", "--skip-format"},
            paramLabel = "<format>",
            description = "Skip subHeader format (${COMPLETION-CANDIDATES}).",
            split = ","
    )
    private List<SubHeaderFormat> skipFormats = new ArrayList<>();

    @Override
    protected void exec() throws Exception {
        if (isSingleFile()) {
            yaml.dump(FileReport.class, createReport(getSingleFile()));
        } else {
            List<FileReport> items = getFiles()
                    .stream()
                    .parallel()
                    .map(asFunction(this::createReport))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            yaml.dumpAll(FileReport.class, items);
        }
    }

    private FileReport createReport(Path file) throws IOException {
        ReportVisitor visitor = new ReportVisitor(skipTypes, skipFormats);
        SasFile.visit(file, visitor);
        return new FileReport(file.toString(), visitor.header, visitor.getPageNodes());
    }

    private boolean testReport(FileReport file) {
        return filter.testScope(file.getHeader())
                && testScope(file.getPages())
                && filter.testKeyValues(file);
    }

    private boolean testScope(List<PageReport> pages) {
        return true;
    }

    @lombok.Value
    public static class FileReport {

        private String file;
        private Header header;
        private List<PageReport> pages;
    }

    @lombok.Value
    public static class PageReport {

        private PageHeader page;
        private List<SubHeaderPointer> subHeaders;
    }

    @lombok.RequiredArgsConstructor
    private static final class ReportVisitor implements SasFileVisitor {

        private final List<PageType> skipTypes;
        private final List<SubHeaderFormat> skipFormats;
        private Header header = null;
        private final Map<PageHeader, List<SubHeaderPointer>> data = new HashMap<>();

        public List<PageReport> getPageNodes() {
            return data
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getKey().getIndex()))
                    .map(entry -> new PageReport(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        @Override
        public FileVisitResult onHeader(Header header) {
            this.header = header;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult onPage(Header header, PageHeader page, BytesReader pageData) {
            if (page.getType().isKnown() && skipTypes.contains(page.getType().get())) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            data.putIfAbsent(page, new ArrayList<>());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult onSubHeaderPointer(Header header, PageHeader page, BytesReader pageData, SubHeaderPointer pointer) {
            if (pointer.getFormat().isKnown() && skipFormats.contains(pointer.getFormat().get())) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            data.get(page).add(pointer);
            return FileVisitResult.CONTINUE;
        }
    }
}
