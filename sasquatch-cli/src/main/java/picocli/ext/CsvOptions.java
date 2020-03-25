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
import nbbrd.picocsv.Csv;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class CsvOptions {

    @CommandLine.Option(
            names = {"-d", "--delimiter"},
            paramLabel = "<char>",
            description = "Delimiting character."
    )
    private char delimiter = Csv.Format.RFC4180.getDelimiter();

    @CommandLine.Option(
            names = {"-n", "--new-line"},
            paramLabel = "<NewLine>",
            description = "NewLine type (${COMPLETION-CANDIDATES})."
    )
    private Csv.NewLine separator = Csv.Format.RFC4180.getSeparator();

    @CommandLine.Option(
            names = {"-q", "--quoter"},
            paramLabel = "<char>",
            description = "Quoting character."
    )
    private char quoter = Csv.Format.RFC4180.getQuote();

    private Csv.Format getFormat() {
        Csv.Format.Builder result = Csv.Format.RFC4180.toBuilder();
        result.delimiter(getDelimiter());
        result.quote(getQuoter());
        result.separator(getSeparator());
        return result.build();
    }

    public Csv.Writer newWriter(Writer charWriter) throws IOException {
        return Csv.Writer.of(charWriter, getFormat());
    }

    public Csv.Reader newReader(Reader charReader) throws IOException {
        return Csv.Reader.of(charReader, getFormat());
    }

    @lombok.Getter
    @lombok.Setter
    public static final class Output extends CsvOptions {

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
        private Charset encoding = RFC4180_ENCODING;

        public Csv.Writer newWriter(Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
            return newWriter(newCharWriter(stdOutEncoding));
        }

        private Writer newCharWriter(Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
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
    public static final class Input extends CsvOptions {

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
        private Charset encoding = RFC4180_ENCODING;

        public Csv.Reader newParser(Supplier<Optional<Charset>> stdInEncoding) throws IOException {
            return newReader(newCharReader(stdInEncoding));
        }

        private Reader newCharReader(Supplier<Optional<Charset>> stdInEncoding) throws IOException {
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

    private static final Charset RFC4180_ENCODING = StandardCharsets.UTF_8;
}
