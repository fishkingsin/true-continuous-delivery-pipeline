package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

public interface Stage {
    String execute(Map<String, Object> config, PipelineContext context);
}
