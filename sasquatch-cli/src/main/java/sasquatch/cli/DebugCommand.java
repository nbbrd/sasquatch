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
package sasquatch.cli;

import internal.cli.BaseCommand;
import nbbrd.console.picocli.PrintContext;
import picocli.CommandLine;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "debug",
        description = "Set of debugging tools.",
        hidden = true,
        subcommands = {
                DebugHeaderCommand.class,
                DebugDocumentCommand.class,
                DebugFileCommand.class,
                DebugCheckCommand.class,
                PrintContext.class
        }
)
@SuppressWarnings("FieldMayBeFinal")
public final class DebugCommand extends BaseCommand {

    @Override
    protected void exec() throws Exception {
        CommandLine.usage(new SasquatchCommand(), System.out);
    }
}
