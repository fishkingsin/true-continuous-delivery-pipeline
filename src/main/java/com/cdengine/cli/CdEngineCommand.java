package com.cdengine.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cdengine", "picocli.spring.boot"})
public class CdEngineCommand implements CommandLineRunner, ExitCodeGenerator {

    @Bean
    public CommandLine myCommand() {
        return new CommandLine(new RootCommand());
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(CdEngineCommand.class, args)
        ));
    }

    @Override
    public void run(String... args) throws Exception {
        int exitCode = new CommandLine(new RootCommand()).execute(args);
        setExitCode(exitCode);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    private int exitCode = 0;

    @Command(name = "cd-engine",
             description = "Enterprise CD Pipeline Engine",
             footer = "Documentation: https://docs.company.com/cd-engine",
             subcommands = {
                 PipelineCommand.class,
                 StageCommand.class,
                 DeployCommand.class,
                 PromoteCommand.class,
                 ConfigCommand.class,
                 VersionCommand.class
             })
    public static class RootCommand implements Runnable {

        @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
        private boolean verbose;

        @CommandLine.Option(names = {"-c", "--config"}, 
                          description = "Config directory path",
                          defaultValue = "config")
        private String configPath;

        @CommandLine.Option(names = {"-h", "--help"}, 
                          usageHelp = true, 
                          description = "Show help")
        private boolean help;

        @Override
        public void run() {
            if (help) {
                CommandLine.usage(this, System.out);
            }
        }
    }
}
