package com.hsbc.ci.engine.core.cli.plugin;

import com.hsbc.ci.engine.core.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginListCommandTest {

    @Test
    void listCommand_usesPluginManagerToListPlugins() {
        PluginManager pluginManager = new TestPluginManager();
        PluginListCommand command = new PluginListCommand(pluginManager);

        command.run();
    }

    static class TestPluginManager extends PluginManager {
        @Override
        public Map<String, Collection<String>> listAllPlugins() {
            Map<String, Collection<String>> result = new HashMap<>();
            result.put("stages", Arrays.asList("security-scan", "performance-test"));
            result.put("gates", Arrays.asList("sonarqube"));
            result.put("notifiers", Arrays.asList("slack-notify", "email-notify"));
            return result;
        }
    }
}
