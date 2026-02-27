package com.hsbc.ci.engine.core.plugin;

import java.util.HashMap;
import java.util.Map;

public interface GatePlugin extends Plugin {
    String getType();
    GateResult evaluate(Map<String, Object> config, Map<String, Object> context);
    
    @Override
    default String getName() { return getType(); }
    
    @Override
    default String getVersion() { return "1.0.0"; }
    
    @Override
    default void init(Map<String, Object> config) {}
    
    @Override
    default PluginResult execute(Map<String, Object> context) {
        GateResult result = evaluate(new HashMap<>(), context);
        return PluginResult.builder()
            .success(result.isPassed())
            .message(result.getMessage())
            .build();
    }
}
