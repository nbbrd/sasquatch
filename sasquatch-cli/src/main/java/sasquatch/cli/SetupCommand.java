package sasquatch.cli;

import internal.cli.BaseCommand;
import nbbrd.console.picocli.GenerateLauncher;
import nbbrd.console.picocli.GenerateRootCompletion;
import picocli.CommandLine;

@CommandLine.Command(
        name = "setup",
        description = "Setup ${PARENT-COMMAND-NAME}.",
        subcommands = {
                GenerateRootCompletion.class,
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
