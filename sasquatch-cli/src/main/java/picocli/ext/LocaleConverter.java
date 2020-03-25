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
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */

package picocli.ext;

import java.util.Locale;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
public final class LocaleConverter implements CommandLine.ITypeConverter<Locale> {

    @Override
    public Locale convert(String string) throws Exception {
        return new Locale.Builder().setLanguageTag(string).build();
    }
}

