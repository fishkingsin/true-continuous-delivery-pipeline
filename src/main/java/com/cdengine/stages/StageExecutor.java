package com.cdengine.stages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdengine.model.PipelineContext;
import com.cdengine.model.StageResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class StageExecutor {

    private final Map<String, Stage> stages = new HashMap<>();

    @Autowired
    private BuildStage buildStage;

    @Autowired
    private TestStage testStage;

    @Autowired
    private ContainerizeStage containerizeStage;

    public StageExecutor() {
    }

    public StageResult execute(String stageType, 
                              Map<String, Object> config, 
                              PipelineContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            Stage stage = getStage(stageType);
            if (stage == null) {
                return StageResult.builder()
                    .stageName(stageType)
                    .success(false)
                    .output("Unknown stage type: " + stageType)
                    .durationMs(0)
                    .build();
            }

            String output = stage.execute(config, context);
            long duration = System.currentTimeMillis() - startTime;

            return StageResult.builder()
                .stageName(stageType)
                .success(true)
                .output(output)
                .durationMs(duration)
                .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return StageResult.builder()
                .stageName(stageType)
                .success(false)
                .output("Error: " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }

    private Stage getStage(String type) {
        return switch (type) {
            case "build" -> buildStage;
            case "test" -> testStage;
            case "containerize" -> containerizeStage;
            default -> null;
        };
    }
}
