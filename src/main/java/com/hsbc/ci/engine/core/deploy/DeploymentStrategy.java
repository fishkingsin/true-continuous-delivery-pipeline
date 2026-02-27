package com.hsbc.ci.engine.core.deploy;

import java.util.Map;

public interface DeploymentStrategy {
    String deploy(String image, String target, Map<String, Object> config);
    String getName();
}
