package com.hsbc.ci.engine.core.cli.plugin;

import com.hsbc.ci.engine.core.plugin.PluginManager;
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

@Component
@CommandLine.Command(name = "list", description = "List available plugins")
class PluginListCommand implements Runnable {

    private final PluginManager pluginManager;

    public PluginListCommand(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void run() {
        var plugins = pluginManager.listAllPlugins();
        
        System.out.println("Available Plugins:");
        System.out.println("");
        
        var stages = plugins.get("stages");
        var gates = plugins.get("gates");
        var notifiers = plugins.get("notifiers");
        
        if (stages != null && !stages.isEmpty()) {
            System.out.println("Stage Plugins:");
            for (String plugin : stages) {
                System.out.println("  " + plugin);
            }
            System.out.println("");
        }
        
        if (gates != null && !gates.isEmpty()) {
            System.out.println("Gate Plugins:");
            for (String plugin : gates) {
                System.out.println("  " + plugin);
            }
            System.out.println("");
        }
        
        if (notifiers != null && !notifiers.isEmpty()) {
            System.out.println("Notifier Plugins:");
            for (String plugin : notifiers) {
                System.out.println("  " + plugin);
            }
        }
        
        if ((stages == null || stages.isEmpty()) 
            && (gates == null || gates.isEmpty()) 
            && (notifiers == null || notifiers.isEmpty())) {
            System.out.println("(No plugins loaded. Edit config/plugins.yml to enable plugins)");
        }
    }
}
