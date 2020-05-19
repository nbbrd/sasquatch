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
import nbbrd.console.picocli.ManifestHelper;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "sasquatch",
        description = "Reader of SAS datasets.",
        versionProvider = SasquatchCommand.ManifestVersionProvider.class,
        subcommands = {
                CsvCommand.class,
                SqlCommand.class,
                DebugCommand.class,
                SetupCommand.class,
                CommandLine.HelpCommand.class
        }
)
public final class SasquatchCommand extends BaseCommand {

    public static void main(String[] args) {
        int exitCode = 0;
        AnsiConsole.systemInstall();
        try {
            CommandLine cli = new CommandLine(new SasquatchCommand());
            cli.setCaseInsensitiveEnumValuesAllowed(true);
            exitCode = cli.execute(args);
        } catch (CommandLine.ExecutionException ex) {
            Logger.getLogger(SasquatchCommand.class.getName()).log(Level.SEVERE, "While executing command", ex);
            System.err.println(ex.getCause().getMessage());
        } finally {
            AnsiConsole.systemUninstall();
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    @Override
    protected void exec() throws Exception {
        CommandLine.usage(new SasquatchCommand(), System.out);
    }

    public static final class ManifestVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            return ManifestHelper.getByTitle("sasquatch-cli")
                    .map(ManifestHelper::getVersion)
                    .orElseGet(() -> new String[0]);
        }
    }
}
