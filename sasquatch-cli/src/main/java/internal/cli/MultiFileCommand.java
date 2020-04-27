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
package internal.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class MultiFileCommand {

    @CommandLine.Parameters(
            paramLabel = "<file>",
            description = "Input SAS7BDAT file(s)",
            arity = "1..*"
    )
    protected List<Path> input;

    @CommandLine.Option(
            names = {"-r", "--recursive"},
            description = "Recursive walking"
    )
    protected boolean recursive = false;

    @CommandLine.Option(
            names = {"-E", "--skip-errors"},
            description = "Skip errors"
    )
    protected boolean skipErrors = false;

    public boolean isSingleFile() {
        return input.size() == 1 && Files.isRegularFile(input.get(0));
    }

    public Path getSingleFile() {
        return input.get(0);
    }

    public List<Path> getFiles() throws IOException {
        List<Path> result = new ArrayList<>();
        for (Path item : input) {
            try ( Stream<Path> files = walk(item, recursive)) {
                files.forEach(result::add);
            }
        }
        return result;
    }

    public <T> Function<Path, Optional<T>> asFunction(IOFunction<Path, T> delegate) {
        return skipErrors
                ? file -> {
                    try {
                        return Optional.ofNullable(delegate.applyWithIO(file));
                    } catch (IOException | RuntimeException ex) {
                        log.log(Level.INFO, "While reading '" + file + "'", ex);
                        return Optional.empty();
                    }
                }
                : file -> {
                    try {
                        return Optional.ofNullable(delegate.applyWithIO(file));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    } catch (RuntimeException ex) {
                        throw ex;
                    }
                };
    }

    public Consumer<Path> asConsumer(IOConsumer<Path> delegate) {
        return skipErrors
                ? file -> {
                    try {
                        delegate.acceptWithIO(file);
                    } catch (IOException | RuntimeException ex) {
                        log.log(Level.INFO, "While reading '" + file + "'", ex);
                    }
                }
                : file -> {
                    try {
                        delegate.acceptWithIO(file);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    } catch (RuntimeException ex) {
                        throw ex;
                    }
                };
    }

    private static Stream<Path> walk(Path path, boolean recursive) throws IOException {
        if (Files.isDirectory(path)) {
            return recursive
                    ? Files.walk(path).filter(MultiFileCommand::isSasFile)
                    : StreamSupport.stream(Files.newDirectoryStream(path, MultiFileCommand::isSasFile).spliterator(), false);
        }
        return Stream.of(path);
    }

    private static boolean isSasFile(Path path) {
        return Files.isRegularFile(path) && path.toString().toLowerCase(Locale.ROOT).endsWith(".sas7bdat");
    }
}
