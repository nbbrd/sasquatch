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
package internal.picocli.yaml;

import nbbrd.console.picocli.text.TextOutputOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.Data
public class YamlOutputOptions extends TextOutputOptions {

    public void dump(Yaml yaml, Object item, Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
        try (Writer writer = newCharWriter(stdOutEncoding)) {
            yaml.dump(item, writer);
        }
    }

    public void dumpAll(Yaml yaml, List<?> items, Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
        try (Writer writer = newCharWriter(stdOutEncoding)) {
            yaml.dump(items, writer);
        }
    }
}
