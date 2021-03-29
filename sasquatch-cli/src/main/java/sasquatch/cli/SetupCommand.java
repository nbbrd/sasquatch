package sasquatch.cli;

import internal.cli.BaseCommand;
import nbbrd.console.picocli.GenerateLauncher;
import picocli.AutoComplete;
import picocli.CommandLine;

@CommandLine.Command(
        name = "setup",
        description = "Setup ${ROOT-COMMAND-NAME}.",
        subcommands = {
                AutoComplete.GenerateCompletion.class,
                GenerateLauncher.class
        }
)
public final class SetupCommand extends BaseCommand {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        spec.commandLine().usage(System.out);
        return null;
    }
}
