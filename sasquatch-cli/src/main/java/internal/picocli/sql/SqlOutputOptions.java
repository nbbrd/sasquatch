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
package internal.picocli.sql;


import nbbrd.console.picocli.text.TextOutputOptions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.Data
public class SqlOutputOptions extends TextOutputOptions {

    public SqlWriter newSqlWriter(Supplier<Optional<Charset>> stdOutEncoding) throws IOException {
        return new SqlWriter(newCharWriter(stdOutEncoding));
    }
}