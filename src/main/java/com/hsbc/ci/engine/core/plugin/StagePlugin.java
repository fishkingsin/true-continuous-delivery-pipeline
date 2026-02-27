package com.hsbc.ci.engine.core.plugin;

import java.util.Map;

public interface StagePlugin extends Plugin {
    String getType();
    void execute(Map<String, Object> config, Map<String, Object> context);
}
