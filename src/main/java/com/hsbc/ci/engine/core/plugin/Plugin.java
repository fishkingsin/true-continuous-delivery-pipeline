package com.hsbc.ci.engine.core.plugin;

import java.util.Map;

public interface Plugin {
    String getName();
    String getVersion();
    void init(Map<String, Object> config);
    PluginResult execute(Map<String, Object> context);
    
    default void cleanup() {}
}
