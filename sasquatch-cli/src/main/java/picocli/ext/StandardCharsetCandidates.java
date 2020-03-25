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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 *
 * @author Philipe Charles
 */
public final class StandardCharsetCandidates implements Iterable<String> {

    @Override
    public Iterator<String> iterator() {
        return Stream.of(
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_8)
                .map(Charset::toString)
                .iterator();
    }
}
