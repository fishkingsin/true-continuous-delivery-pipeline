package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.config.EnvironmentLoader;
import com.hsbc.ci.engine.core.model.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;

import java.util.Collection;

@Command(name = "config", description = "Manage configuration")
public class ConfigCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigCommand.class);

    @Autowired
    private EnvironmentLoader environmentLoader;

    @Override
    public void run() {
        System.out.println("Usage: config <subcommand>");
        System.out.println("Subcommands:");
        System.out.println("  env, environment  Manage environments");
        System.out.println("  run              Run configuration");
    }

    @Command(name = "env", description = "Manage environments")
    public static class EnvironmentSubCommand implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(EnvironmentSubCommand.class);

        @Autowired
        private EnvironmentLoader environmentLoader;

        @Override
        public void run() {
            listEnvironments();
        }

        @Command(name = "list", description = "List all environments")
        public void listEnvironments() {
            Collection<Environment> envs = environmentLoader.listEnvironments();
            if (envs.isEmpty()) {
                System.out.println("No environments configured");
                return;
            }
            System.out.println("Configured Environments:");
            System.out.println("-----------------------");
            for (Environment env : environmentLoader.getOrderedEnvironments()) {
                System.out.printf("  %s%n", env.getName());
                if (env.getDescription() != null) {
                    System.out.printf("    Description: %s%n", env.getDescription());
                }
                if (env.getOrder() != null) {
                    System.out.printf("    Order: %d%n", env.getOrder());
                }
                if (env.getDeploy() != null) {
                    System.out.printf("    Deploy: type=%s, namespace=%s, cluster=%s%n",
                        env.getDeploy().getType(),
                        env.getDeploy().getNamespace(),
                        env.getDeploy().getCluster());
                }
                if (env.getAutoPromote() != null) {
                    System.out.printf("    Auto-promote: %s%n", env.getAutoPromote());
                }
            }
        }

        @Command(name = "show", description = "Show environment details")
        public void showEnvironment(String name) {
            Environment env = environmentLoader.getEnvironment(name);
            if (env == null) {
                System.out.println("Environment not found: " + name);
                return;
            }
            System.out.println("Environment: " + env.getName());
            System.out.println("--------------");
            System.out.printf("Description: %s%n", env.getDescription());
            System.out.printf("Order: %s%n", env.getOrder());
            System.out.printf("Auto-promote: %s%n", env.getAutoPromote());
            
            if (env.getDeploy() != null) {
                System.out.println("Deploy Config:");
                System.out.printf("  Type: %s%n", env.getDeploy().getType());
                System.out.printf("  Namespace: %s%n", env.getDeploy().getNamespace());
                System.out.printf("  Cluster: %s%n", env.getDeploy().getCluster());
                System.out.printf("  Strategy: %s%n", env.getDeploy().getStrategy());
            }
            
            if (env.getApproval() != null) {
                System.out.println("Approval:");
                System.out.printf("  Type: %s%n", env.getApproval().getType());
                System.out.printf("  Roles: %s%n", env.getApproval().getRoles());
                System.out.printf("  Timeout: %s%n", env.getApproval().getTimeout());
            }
            
            if (env.getGates() != null && !env.getGates().isEmpty()) {
                System.out.printf("Gates: %s%n", String.join(", ", env.getGates()));
            }
            
            if (env.getResources() != null) {
                System.out.println("Resources:");
                System.out.printf("  CPU: %s%n", env.getResources().getCpu());
                System.out.printf("  Memory: %s%n", env.getResources().getMemory());
            }
            
            boolean valid = environmentLoader.validate(name);
            System.out.printf("Valid: %s%n", valid);
        }

        @Command(name = "validate", description = "Validate environment configuration")
        public void validateEnvironment(String name) {
            boolean valid = environmentLoader.validate(name);
            if (valid) {
                System.out.println("Environment is valid: " + name);
            } else {
                System.out.println("Environment is invalid: " + name);
            }
        }

        @Command(name = "promote-chain", description = "Show promotion chain from environment")
        public void showPromotionChain(String from) {
            var chain = environmentLoader.getPromotionChain(from);
            if (chain.isEmpty()) {
                System.out.println("No promotion chain found for: " + from);
                return;
            }
            System.out.println("Promotion Chain from " + from + ":");
            for (int i = 0; i < chain.size(); i++) {
                Environment env = chain.get(i);
                String marker = env.getName().equals(from) ? "* " : "  ";
                System.out.printf("%s%s (order: %d)%n", marker, env.getName(), env.getOrder());
            }
        }
    }

    @Command(name = "environment", description = "Manage environments (alias for env)")
    public static class EnvironmentCommand implements Runnable {

        @Autowired
        private EnvironmentLoader environmentLoader;

        @Override
        public void run() {
            Collection<Environment> envs = environmentLoader.listEnvironments();
            if (envs.isEmpty()) {
                System.out.println("No environments configured");
                return;
            }
            for (Environment env : environmentLoader.getOrderedEnvironments()) {
                System.out.println(env.getName());
            }
        }
    }
}
