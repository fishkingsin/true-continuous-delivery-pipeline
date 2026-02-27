package com.hsbc.ci.engine.core.cli.plugin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(name = "plugin", 
                     description = "Plugin management",
                     subcommands = {PluginListCommand.class})
@Component
public class PluginCommand implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
    }
}

@CommandLine.Command(name = "list", description = "List available plugins")
@Component
class PluginListCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Available Plugins:");
        System.out.println("");
        System.out.println("(Edit config/plugins.yml to enable plugins)");
        System.out.println("");
        System.out.println("Stage Plugins:");
        System.out.println("  security-scan      - SAST, DAST, FOSS scanning");
        System.out.println("  performance-test  - K6/JMeter load testing");
        System.out.println("  chaos-engineering - Litmus chaos experiments");
        System.out.println("");
        System.out.println("Gate Plugins:");
        System.out.println("  sonarqube         - Code quality gates");
        System.out.println("  security-gate     - Security scan gates");
        System.out.println("");
        System.out.println("Notifier Plugins:");
        System.out.println("  slack-notify      - Slack notifications");
        System.out.println("  email-notify      - Email notifications");
    }
}
