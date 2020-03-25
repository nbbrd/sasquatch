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
 * See the Licence of the specific language governing permissions and 
 * limitations under the Licence.
 */
package picocli.ext;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Supplier;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class CharOptions {

    @lombok.Getter
    @lombok.Setter
    public static final class Output extends CharOptions {

        @CommandLine.Option(
                names = {"-o", "--output-file"},
                paramLabel = "<file>",
                description = "Output to a file instead of stdout."
        )
        private Path file = null;

        @CommandLine.Option(
                names = {"-e", "--encoding"},
                paramLabel = "<encoding>",
                description = "Charset used to encode text.",
                completionCandidates = StandardCharsetCandidates.class
        )
        private Charset encoding = StandardCharsets.UTF_8;

        public Writer newCharWriter(Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
            return file != null
                    ? new OutputStreamWriter(
                            Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                            getEncoding()
                    )
                    : new OutputStreamWriter(
                            new UncloseableOutputStream(System.out),
                            stdOutEncoding.get().orElse(StandardCharsets.UTF_8)
                    );
        }
    }

    @lombok.AllArgsConstructor
    private static final class UncloseableOutputStream extends OutputStream {

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final OutputStream delegate;
    }

    @lombok.Getter
    @lombok.Setter
    public static final class Input extends CharOptions {

        @CommandLine.Option(
                names = {"-i", "--input-file"},
                paramLabel = "<file>",
                description = "Output to a file instead of stdout."
        )
        private Path file = null;

        @CommandLine.Option(
                names = {"-e", "--encoding"},
                paramLabel = "<encoding>",
                description = "Charset used to encode text.",
                completionCandidates = StandardCharsetCandidates.class
        )
        private Charset encoding = StandardCharsets.UTF_8;

        public Reader newCharReader(Supplier<Optional<Charset>> stdInEncoding) throws IOException {
            return file != null
                    ? new InputStreamReader(
                            Files.newInputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                            getEncoding()
                    )
                    : new InputStreamReader(
                            new UncloseableInputStream(System.in),
                            stdInEncoding.get().orElse(StandardCharsets.UTF_8)
                    );
        }
    }

    @lombok.AllArgsConstructor
    private static final class UncloseableInputStream extends InputStream {

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final InputStream delegate;
    }
}
