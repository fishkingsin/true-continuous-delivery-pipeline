package com.hsbc.ci.engine.core.plugin;

import java.util.Map;

public interface GatePlugin extends Plugin {
    String getType();
    GateResult evaluate(Map<String, Object> config, Map<String, Object> context);
}
