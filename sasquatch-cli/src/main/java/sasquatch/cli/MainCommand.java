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
import nbbrd.console.picocli.ConfigHelper;
import nbbrd.console.picocli.LoggerHelper;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;
import sasquatch.About;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = About.NAME,
        description = "Reader of SAS datasets.",
        versionProvider = MainCommand.ManifestVersionProvider.class,
        subcommands = {
                CsvCommand.class,
                SqlCommand.class,
                DebugCommand.class,
                SetupCommand.class,
                CommandLine.HelpCommand.class
        }
)
public final class MainCommand extends BaseCommand {

    public static void main(String[] args) {
        ConfigHelper.of(About.NAME).loadAll(System.getProperties());
        LoggerHelper.disableDefaultConsoleLogger();

        int exitCode = 0;
        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            CommandLine cli = new CommandLine(new MainCommand());
            cli.setCaseInsensitiveEnumValuesAllowed(true);
            exitCode = cli.execute(args);
        } catch (CommandLine.ExecutionException ex) {
            Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, "While executing command", ex);
            System.err.println(ex.getCause().getMessage());
        }
        System.exit(exitCode);
    }

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        spec.commandLine().usage(System.out);
        return null;
    }

    public static final class ManifestVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            return new String[]{
                    "@|bold " + About.NAME + " " + About.VERSION + "|@",
                    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                    "OS: ${os.name} ${os.version} ${os.arch}"
            };
        }
    }
}
