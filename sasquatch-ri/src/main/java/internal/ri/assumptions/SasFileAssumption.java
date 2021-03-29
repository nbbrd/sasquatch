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
package internal.ri.assumptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Philippe Charles
 */
public interface SasFileAssumption {

    String getName();

    String test(SeekableByteChannel file, SasFileStructure structure) throws IOException;

    @ServiceDefinition(quantifier = Quantifier.MULTIPLE, singleton = true)
    interface Provider {

        Collection<? extends SasFileAssumption> getAssumptions();
    }

    static Stream<SasFileAssumption> getAll() {
        return SasFileAssumptionLoader.Provider.get().stream().flatMap(provider -> provider.getAssumptions().stream());
    }

    static void testAll(Path file, Consumer<SasFileError> onError) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(file, READ)) {
            SasFileStructure structure = SasFileStructure.of(sbc);
            for (Iterator<SasFileAssumption> iter = getAll().iterator(); iter.hasNext();) {
                SasFileAssumption assumption = iter.next();
                try {
                    String error = assumption.test(sbc, structure);
                    if (error != null) {
                        onError.accept(new SasFileError(file, assumption, error));
                    }
                } catch (IOException ex) {
                    onError.accept(new SasFileError(file, assumption, ex.getMessage()));
                    break;
                }
            }
        } catch (RuntimeException ex) {
            if (ex instanceof UncheckedIOException) {
                throw ((UncheckedIOException) ex).getCause();
            }
            throw new IOException(file.toString(), ex);
        }
    }
}
