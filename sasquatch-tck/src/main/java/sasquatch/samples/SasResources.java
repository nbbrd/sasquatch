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
package sasquatch.samples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
public enum SasResources {

    DUMBMATTER(Paths.get("github_dumbmatter", "test", "data")),
    EPAM(Paths.get("github_epam", "src", "test", "resources", "sas7bdat")),
    KSHEDDEN(Paths.get("github_kshedden", "test_files", "data")),
    PPHAM27(Paths.get("github_ppham27", "test_files")),
    TK3369(Paths.get("github_tk3369", "test"));

    @lombok.Getter
    private final Path root;

    private SasResources(Path relative) {
        this.root = getSasTestFiles().resolve(relative);
    }

    private static Path getSasTestFiles() {
        String sasTestFiles = System.getenv("sas_test_files");
        if (sasTestFiles == null) {
            throw new RuntimeException("Env variable 'sas_test_files' not found");
        }
        Path result = Paths.get(sasTestFiles);
        if (!Files.isDirectory(result)) {
            throw new RuntimeException("'sas_test_files' is not a directory");
        }
        return result;
    }

    public static Stream<Path> all() {
        return Stream.of(SasResources.values())
                .map(SasResources::getRoot)
                .flatMap(SasResources::walk);
    }

    public static Stream<Path> walk(Path folder) {
        try {
            return Files.walk(folder).filter(o -> !Files.isDirectory(o) && o.toString().endsWith(".sas7bdat"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static final Path LITTLE_32 = PPHAM27.getRoot().resolve("states.sas7bdat");

    public static final Path LITTLE_64 = KSHEDDEN.getRoot().resolve("test7.sas7bdat");
    public static final Path LITTLE_64_BIN = KSHEDDEN.getRoot().resolve("test8.sas7bdat");
    public static final Path LITTLE_64_CHAR = KSHEDDEN.getRoot().resolve("test9.sas7bdat");

    public static final Path BIG_32 = KSHEDDEN.getRoot().resolve("test10.sas7bdat");
    public static final Path BIG_64 = KSHEDDEN.getRoot().resolve("test13.sas7bdat");
}
