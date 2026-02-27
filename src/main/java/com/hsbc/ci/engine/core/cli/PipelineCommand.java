package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "pipeline", description = "Manage pipelines", subcommands = {
    PipelineCommand.Run.class,
    PipelineCommand.List.class,
    PipelineCommand.Validate.class
})
public class PipelineCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PipelineCommand.class);

    @Override
    public void run() {
    }

    @Command(name = "run", description = "Run a pipeline")
    public static class Run implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(Run.class);
        
        @Override
        public void run() {
            log.info("Pipeline run not yet implemented");
            System.out.println("Pipeline run not yet implemented");
        }
    }

    @Command(name = "list", description = "List pipelines")
    public static class List implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(List.class);
        
        @Override
        public void run() {
            log.info("Pipeline list not yet implemented");
            System.out.println("Pipeline list not yet implemented");
        }
    }

    @Command(name = "validate", description = "Validate pipeline")
    public static class Validate implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(Validate.class);
        
        @Override
        public void run() {
            log.info("Pipeline validate not yet implemented");
            System.out.println("Pipeline validate not yet implemented");
        }
    }
}
