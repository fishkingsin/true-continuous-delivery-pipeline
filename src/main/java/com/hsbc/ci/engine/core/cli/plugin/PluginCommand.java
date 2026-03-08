package com.hsbc.ci.engine.core.cli.plugin;

import com.hsbc.ci.engine.core.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;

@CommandLine.Command(name = "plugin", 
                     description = "Plugin management")
@Component
public class PluginCommand implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PluginCommand.class);
    
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ConsoleOutput console;

    public PluginCommand() {
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "list".equals(args[0])) {
            listPlugins();
        }
    }

    @CommandLine.Command(name = "list", description = "List available plugins")
    public void listPlugins() {
        var plugins = pluginManager.listAllPlugins();
        
        console.print("Available Plugins:");
        console.print("");
        
        var stages = plugins.get("stages");
        var gates = plugins.get("gates");
        var notifiers = plugins.get("notifiers");
        
        if (stages != null && !stages.isEmpty()) {
            console.print("Stage Plugins:");
            for (String plugin : stages) {
                console.print("  " + plugin);
            }
            console.print("");
        }
        
        if (gates != null && !gates.isEmpty()) {
            console.print("Gate Plugins:");
            for (String plugin : gates) {
                console.print("  " + plugin);
            }
            console.print("");
        }
        
        if (notifiers != null && !notifiers.isEmpty()) {
            console.print("Notifier Plugins:");
            for (String plugin : notifiers) {
                console.print("  " + plugin);
            }
        }
        
        if ((stages == null || stages.isEmpty()) 
            && (gates == null || gates.isEmpty()) 
            && (notifiers == null || notifiers.isEmpty())) {
            console.print("(No plugins loaded. Edit config/plugins.yml to enable plugins)");
        }
        
        log.debug("Listed plugins: stages={}, gates={}, notifiers={}", 
            stages != null ? stages.size() : 0,
            gates != null ? gates.size() : 0,
            notifiers != null ? notifiers.size() : 0);
    }
}
