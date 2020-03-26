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
package internal.cli;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.Callable;
import nbbrd.console.properties.ConsoleProperties;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
@CommandLine.Command(
        sortOptions = false,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        headerHeading = "%n",
        mixinStandardHelpOptions = true,
        showDefaultValues = true
)
public abstract class BaseCommand implements Callable<Void> {

    @Override
    final public Void call() throws Exception {
        exec();
        return null;
    }

    abstract protected void exec() throws Exception;
    

    public Optional<Charset> getStdOutEncoding() {
        return ConsoleProperties.ofServiceLoader().getStdOutEncoding();
    }
}
