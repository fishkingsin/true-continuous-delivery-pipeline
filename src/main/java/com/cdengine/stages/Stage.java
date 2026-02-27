package com.cdengine.stages;

import com.cdengine.model.PipelineContext;
import java.util.Map;

public interface Stage {
    String execute(Map<String, Object> config, PipelineContext context);
}
