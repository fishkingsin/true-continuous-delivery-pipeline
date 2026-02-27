package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.CiEngineApplication;
import com.hsbc.ci.engine.core.cli.checkout.CheckoutCommand;
import com.hsbc.ci.engine.core.cli.build.BuildCommand;
import com.hsbc.ci.engine.core.cli.plugin.PluginCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hsbc.ci.engine.core"})
public class CiEngineCommand implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CiEngineCommand.class);

    private final ApplicationContext context;
    private int exitCode = 0;

    public CiEngineCommand(ApplicationContext context) {
        this.context = context;
    }

    public static void main(String[] args) {
        try {
            System.exit(SpringApplication.exit(
                SpringApplication.run(CiEngineApplication.class, args)
            ));
        } catch (ParameterException e) {
            System.err.println("\n[ERROR] Invalid command or option: " + e.getMessage());
            System.err.println("\nUsage: java -jar ci-engine-core-1.0.0-SNAPSHOT.jar <command> [options]");
            System.err.println("\nExamples:");
            System.err.println("  java -jar ci-engine-core-1.0.0-SNAPSHOT.jar --help");
            System.err.println("  java -jar ci-engine-core-1.0.0-SNAPSHOT.jar version");
            System.err.println("  java -jar ci-engine-core-1.0.0-SNAPSHOT.jar deploy --type kubernetes --namespace dev --image myapp:1.0.0");
            System.err.println("\nRun 'java -jar ci-engine-core-1.0.0-SNAPSHOT.jar --help' for more information.");
            System.exit(1);
        } catch (Exception e) {
            log.error("CI Engine error", e);
            System.err.println("\n[ERROR] " + e.getMessage());
            System.err.println("\nRun 'java -jar ci-engine-core-1.0.0-SNAPSHOT.jar --help' for usage information.");
            System.exit(1);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        RootCommand root = new RootCommand();
        CommandLine commandLine = new CommandLine(root)
            .addSubcommand("pipeline", context.getBean(PipelineCommand.class))
            .addSubcommand("stage", new StageCommand())
            .addSubcommand("deploy", context.getBean(DeployCommand.class))
            .addSubcommand("promote", new PromoteCommand())
            .addSubcommand("config", new ConfigCommand())
            .addSubcommand("version", new VersionCommand())
            .addSubcommand("checkout", context.getBean(CheckoutCommand.class))
            .addSubcommand("build", context.getBean(BuildCommand.class))
            .addSubcommand("plugin", context.getBean(PluginCommand.class));
        
        exitCode = commandLine.execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Command(name = "ci-engine",
             description = "Enterprise CD Pipeline Engine",
             footer = "Documentation: https://docs.company.com/ci-engine")
    public static class RootCommand implements Runnable {

        @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
        private boolean verbose;

        @CommandLine.Option(names = {"-c", "--config"}, 
                          description = "Config directory path",
                          paramLabel = "<path>")
        private String configPath = "config";

        @CommandLine.Option(names = {"-h", "--help"}, 
                          usageHelp = true, 
                          description = "Show help")
        private boolean help;

        @Override
        public void run() {
            if (help) {
                CommandLine.usage(this, System.out);
            } else {
                CommandLine.usage(this, System.out);
            }
        }
    }
}
