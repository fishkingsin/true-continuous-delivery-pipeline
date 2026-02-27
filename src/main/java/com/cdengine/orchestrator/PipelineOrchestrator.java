package com.cdengine.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdengine.config.ConfigurationLoader;
import com.cdengine.model.PipelineContext;
import com.cdengine.model.PipelineResult;
import com.cdengine.model.StageResult;
import com.cdengine.stages.StageExecutor;

import java.util.List;
import java.util.Map;

@Component
public class PipelineOrchestrator {

    @Autowired
    private ConfigurationLoader configLoader;

    @Autowired
    private StageExecutor stageExecutor;

    public PipelineResult execute(PipelineContext context) {
        String pipelineName = context.getPipelineName();
        Map<String, Object> pipelineConfig = configLoader.getPipeline(pipelineName);

        if (pipelineConfig == null) {
            return PipelineResult.failed("Pipeline not found: " + pipelineName);
        }

        if (context.isDryRun()) {
            System.out.println("[DRY-RUN] Would execute pipeline: " + pipelineName);
            return PipelineResult.success(context);
        }

        System.out.println("[INFO] Executing pipeline: " + pipelineName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stages = (List<Map<String, Object>>) pipelineConfig.get("stages");

        if (stages == null) {
            return PipelineResult.failed("No stages defined in pipeline");
        }

        for (Map<String, Object> stageConfig : stages) {
            String stageName = (String) stageConfig.get("name");
            String stageType = (String) stageConfig.get("type");

            System.out.println("[INFO] Executing stage: " + stageName);

            StageResult result = stageExecutor.execute(stageType, stageConfig, context);
            context.addStageResult(stageName, result);

            if (!result.isSuccess()) {
                return PipelineResult.failed("Stage failed: " + stageName);
            }
        }

        return PipelineResult.success(context);
    }
}
