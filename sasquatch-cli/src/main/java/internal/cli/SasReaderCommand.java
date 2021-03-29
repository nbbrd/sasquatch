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

import picocli.CommandLine;
import sasquatch.Sasquatch;
import sasquatch.ri.SasquatchReader;
import sasquatch.spi.SasReader;
import sasquatch.spi.SasReaderLoader;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public abstract class SasReaderCommand extends BaseCommand {

    @CommandLine.Option(
            names = {"--engine"},
            paramLabel = "<name>",
            description = "Engine name (${COMPLETION-CANDIDATES}).",
            completionCandidates = SasReaderCandidates.class
    )
    protected String engine = SasquatchReader.NAME;

    public Sasquatch getSasquatch() throws IOException {
        return SasReaderLoader.load()
                .stream()
                .filter(reader -> engine.equals(reader.getName()))
                .map(Sasquatch::of)
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find engine '" + engine + "'"));
    }

    public static final class SasReaderCandidates implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return SasReaderLoader.load()
                    .stream()
                    .map(SasReader::getName)
                    .iterator();
        }
    }
}
