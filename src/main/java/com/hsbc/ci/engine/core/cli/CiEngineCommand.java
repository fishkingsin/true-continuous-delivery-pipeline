package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.CiEngineApplication;
import com.hsbc.ci.engine.core.cli.checkout.CheckoutCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hsbc.ci.engine.core"})
public class CiEngineCommand implements CommandLineRunner, ExitCodeGenerator {

    private int exitCode = 0;

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(CiEngineApplication.class, args)
        ));
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(new RootCommand()).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Command(name = "ci-engine",
             description = "Enterprise CD Pipeline Engine",
             footer = "Documentation: https://docs.company.com/ci-engine",
             subcommands = {
                 PipelineCommand.class,
                 StageCommand.class,
                 DeployCommand.class,
                 PromoteCommand.class,
                 ConfigCommand.class,
                 VersionCommand.class,
                 CheckoutCommand.class
             })
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
