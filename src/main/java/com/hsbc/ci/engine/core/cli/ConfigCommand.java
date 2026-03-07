package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import com.hsbc.ci.engine.core.config.EnvironmentLoader;
import com.hsbc.ci.engine.core.model.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.Collection;

@Command(name = "config", description = "Manage configuration")
@Component
public class ConfigCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigCommand.class);

    @Autowired
    private EnvironmentLoader environmentLoader;

    @Autowired
    private ConsoleOutput console;

    @Override
    public void run() {
        console.print("Usage: config <subcommand>");
        console.print("Subcommands:");
        console.print("  env, environment  Manage environments");
        console.print("  run              Run configuration");
    }

    @Command(name = "env", description = "Manage environments")
    public static class EnvironmentSubCommand implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(EnvironmentSubCommand.class);

        @Autowired
        private EnvironmentLoader environmentLoader;

        @Autowired
        private ConsoleOutput console;

        @Override
        public void run() {
            listEnvironments();
        }

        @Command(name = "list", description = "List all environments")
        public void listEnvironments() {
            Collection<Environment> envs = environmentLoader.listEnvironments();
            if (envs.isEmpty()) {
                console.print("No environments configured");
                return;
            }
            console.print("Configured Environments:");
            console.print("-----------------------");
            for (Environment env : environmentLoader.getOrderedEnvironments()) {
                console.printf("  %s%n", env.getName());
                if (env.getDescription() != null) {
                    console.printf("    Description: %s%n", env.getDescription());
                }
                if (env.getOrder() != null) {
                    console.printf("    Order: %d%n", env.getOrder());
                }
                if (env.getDeploy() != null) {
                    console.printf("    Deploy: type=%s, namespace=%s, cluster=%s%n",
                        env.getDeploy().getType(),
                        env.getDeploy().getNamespace(),
                        env.getDeploy().getCluster());
                }
                if (env.getAutoPromote() != null) {
                    console.printf("    Auto-promote: %s%n", env.getAutoPromote());
                }
            }
        }

        @Command(name = "show", description = "Show environment details")
        public void showEnvironment(String name) {
            Environment env = environmentLoader.getEnvironment(name);
            if (env == null) {
                console.print("Environment not found: " + name);
                return;
            }
            console.print("Environment: " + env.getName());
            console.print("--------------");
            console.printf("Description: %s%n", env.getDescription());
            console.printf("Order: %s%n", env.getOrder());
            console.printf("Auto-promote: %s%n", env.getAutoPromote());
            
            if (env.getDeploy() != null) {
                console.print("Deploy Config:");
                console.printf("  Type: %s%n", env.getDeploy().getType());
                console.printf("  Namespace: %s%n", env.getDeploy().getNamespace());
                console.printf("  Cluster: %s%n", env.getDeploy().getCluster());
                console.printf("  Strategy: %s%n", env.getDeploy().getStrategy());
            }
            
            if (env.getApproval() != null) {
                console.print("Approval:");
                console.printf("  Type: %s%n", env.getApproval().getType());
                console.printf("  Roles: %s%n", env.getApproval().getRoles());
                console.printf("  Timeout: %s%n", env.getApproval().getTimeout());
            }
            
            if (env.getGates() != null && !env.getGates().isEmpty()) {
                console.printf("Gates: %s%n", String.join(", ", env.getGates()));
            }
            
            if (env.getResources() != null) {
                console.print("Resources:");
                console.printf("  CPU: %s%n", env.getResources().getCpu());
                console.printf("  Memory: %s%n", env.getResources().getMemory());
            }
            
            boolean valid = environmentLoader.validate(name);
            console.printf("Valid: %s%n", valid);
        }

        @Command(name = "validate", description = "Validate environment configuration")
        public void validateEnvironment(String name) {
            boolean valid = environmentLoader.validate(name);
            if (valid) {
                console.print("Environment is valid: " + name);
            } else {
                console.print("Environment is invalid: " + name);
            }
        }

        @Command(name = "promote-chain", description = "Show promotion chain from environment")
        public void showPromotionChain(String from) {
            var chain = environmentLoader.getPromotionChain(from);
            if (chain.isEmpty()) {
                console.print("No promotion chain found for: " + from);
                return;
            }
            console.print("Promotion Chain from " + from + ":");
            for (int i = 0; i < chain.size(); i++) {
                Environment env = chain.get(i);
                String marker = env.getName().equals(from) ? "* " : "  ";
                console.printf("%s%s (order: %d)%n", marker, env.getName(), env.getOrder());
            }
        }
    }

    @Command(name = "environment", description = "Manage environments (alias for env)")
    public static class EnvironmentCommand implements Runnable {

        @Autowired
        private EnvironmentLoader environmentLoader;

        @Autowired
        private ConsoleOutput console;

        @Override
        public void run() {
            Collection<Environment> envs = environmentLoader.listEnvironments();
            if (envs.isEmpty()) {
                console.print("No environments configured");
                return;
            }
            for (Environment env : environmentLoader.getOrderedEnvironments()) {
                console.print(env.getName());
            }
        }
    }
}
