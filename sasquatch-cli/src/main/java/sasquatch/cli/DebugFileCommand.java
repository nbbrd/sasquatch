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
import internal.cli.BaseCommand;
import internal.cli.DebugFilterOptions;
import internal.cli.DebugOutputOptions;
import internal.ri.base.*;
import nbbrd.console.picocli.MultiFileInputOptions;
import picocli.CommandLine;
import sasquatch.util.SasFilenameFilter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "file"
)
@SuppressWarnings("FieldMayBeFinal")
@lombok.extern.java.Log
public final class DebugFileCommand extends BaseCommand {

    @CommandLine.Mixin
    private MultiFileInputOptions input = new MultiFileInputOptions();

    @CommandLine.ArgGroup(validate = false, heading = "%nFilter options:%n")
    private DebugFilterOptions filter = new DebugFilterOptions();

    @CommandLine.ArgGroup
    private DebugOutputOptions output = new DebugOutputOptions();

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
    public Void call() throws Exception {
        if (input.isSingleFile()) {
            output.dump(FileReport.class, createReport(input.getSingleFile()), this::getStdOutEncoding);
        } else {
            List<FileReport> items = input.getAllFiles(new SasFilenameFilter()::accept)
                    .stream()
                    .parallel()
                    .map(input.asFunction(this::createReport, this::log))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::testReport)
                    .collect(Collectors.toList());
            output.dumpAll(FileReport.class, items, this::getStdOutEncoding);
        }
        return null;
    }

    private void log(Exception ex, Path file) {
        log.log(Level.INFO, "While reading '" + file + "'", ex);
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
