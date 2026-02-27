package com.hsbc.ci.engine.core.cli;

import picocli.CommandLine.Command;

@Command(name = "pipeline", description = "Manage pipelines", subcommands = {
    PipelineCommand.Run.class,
    PipelineCommand.List.class,
    PipelineCommand.Validate.class
})
public class PipelineCommand implements Runnable {

    @Override
    public void run() {
    }

    @Command(name = "run", description = "Run a pipeline")
    public static class Run implements Runnable {
        @Override
        public void run() {
            System.out.println("Pipeline run not yet implemented");
        }
    }

    @Command(name = "list", description = "List pipelines")
    public static class List implements Runnable {
        @Override
        public void run() {
            System.out.println("Pipeline list not yet implemented");
        }
    }

    @Command(name = "validate", description = "Validate pipeline")
    public static class Validate implements Runnable {
        @Override
        public void run() {
            System.out.println("Pipeline validate not yet implemented");
        }
    }
}
