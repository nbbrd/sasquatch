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
    CommandLine.Model.CommandSpec spec;

    @Override
    protected void exec() throws Exception {
        spec.commandLine().usage(System.out);
    }
}
