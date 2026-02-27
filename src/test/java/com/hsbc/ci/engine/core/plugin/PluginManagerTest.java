package com.hsbc.ci.engine.core.plugin;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginManagerTest {

    @Test
    void registerStage_addsPluginToList() {
        PluginManager manager = new PluginManager();
        
        manager.registerStage(new TestStagePlugin("security-scan"));
        
        Collection<String> plugins = manager.listStagePlugins();
        assertTrue(plugins.contains("security-scan"));
    }

    @Test
    void registerGate_addsPluginToList() {
        PluginManager manager = new PluginManager();
        
        manager.registerGate(new TestGatePlugin("sonarqube"));
        
        Collection<String> plugins = manager.listGatePlugins();
        assertTrue(plugins.contains("sonarqube"));
    }

    @Test
    void registerNotifier_addsPluginToList() {
        PluginManager manager = new PluginManager();
        
        manager.registerNotifier(new TestNotifierPlugin("slack-notify"));
        
        Collection<String> plugins = manager.listNotifierPlugins();
        assertTrue(plugins.contains("slack-notify"));
    }

    @Test
    void listAllPlugins_returnsAllTypes() {
        PluginManager manager = new PluginManager();
        
        manager.registerStage(new TestStagePlugin("security-scan"));
        manager.registerGate(new TestGatePlugin("sonarqube"));
        manager.registerNotifier(new TestNotifierPlugin("slack-notify"));
        
        var all = manager.listAllPlugins();
        
        assertTrue(all.get("stages").contains("security-scan"));
        assertTrue(all.get("gates").contains("sonarqube"));
        assertTrue(all.get("notifiers").contains("slack-notify"));
    }

    static class TestStagePlugin implements StagePlugin {
        private final String name;
        
        TestStagePlugin(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getVersion() { return "1.0.0"; }
        
        @Override
        public void init(Map<String, Object> config) {}
        
        @Override
        public String getType() { return "stage"; }
        
        @Override
        public void execute(Map<String, Object> config, Map<String, Object> context) {}
        
        @Override
        public PluginResult execute(Map<String, Object> context) {
            return PluginResult.success("Test plugin executed");
        }
    }

    static class TestGatePlugin implements GatePlugin {
        private final String name;
        
        TestGatePlugin(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getVersion() { return "1.0.0"; }
        
        @Override
        public void init(Map<String, Object> config) {}
        
        @Override
        public String getType() { return "gate"; }
        
        @Override
        public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
            return GateResult.pass("Test gate passed");
        }

        @Override
        public PluginResult execute(Map<String, Object> context) {
            return PluginResult.success("Test gate executed");
        }
    }

    static class TestNotifierPlugin implements NotifierPlugin {
        private final String name;
        
        TestNotifierPlugin(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getVersion() { return "1.0.0"; }
        
        @Override
        public void init(Map<String, Object> config) {}
        
        @Override
        public String getType() { return "notifier"; }
        
        @Override
        public void notify(Notification notification) {}

        @Override
        public PluginResult execute(Map<String, Object> context) {
            return PluginResult.success("Test notifier executed");
        }
    }
}
