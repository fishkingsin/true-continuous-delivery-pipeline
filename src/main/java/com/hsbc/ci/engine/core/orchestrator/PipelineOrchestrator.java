package com.hsbc.ci.engine.core.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import com.hsbc.ci.engine.core.stages.StageExecutor;
import com.hsbc.ci.engine.core.config.PipelineValidator;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class PipelineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PipelineOrchestrator.class);

    private ConfigurationLoader configLoader;
    private StageExecutor stageExecutor;
    private PluginManager pluginManager;
    private PipelineValidator pipelineValidator;

    public PipelineOrchestrator() {
    }

    @Autowired
    public PipelineOrchestrator(ConfigurationLoader configLoader, 
                                StageExecutor stageExecutor,
                                PluginManager pluginManager) {
        this.configLoader = configLoader;
        this.stageExecutor = stageExecutor;
        this.pluginManager = pluginManager;
        this.pipelineValidator = new PipelineValidator();
    }

    public PipelineResult execute(PipelineContext context) {
        String pipelineName = context.getPipelineName();
        
        PipelineDefinition pipelineDef = configLoader.loadPipelineDefinition(pipelineName);

        if (pipelineDef == null) {
            log.error("Pipeline not found: {}", pipelineName);
            return PipelineResult.failed("Pipeline not found: " + pipelineName);
        }

        PipelineValidator.ValidationResult validationResult = pipelineValidator.validate(pipelineDef);
        if (!validationResult.isValid()) {
            log.error("Pipeline validation failed: {}", validationResult.getErrors());
            return PipelineResult.failed("Pipeline validation failed: " + String.join(", ", validationResult.getErrors()));
        }

        if (context.isDryRun()) {
            log.info("DRY-RUN: Would execute pipeline: {}", pipelineName);
            return PipelineResult.success(context);
        }

        log.info("Executing pipeline: {}", pipelineName);

        executePreStagePlugins(context);

        List<StageDefinition> stages = pipelineDef.getStages();
        
        PipelineResult result = executeStages(stages, context);

        executePostStagePlugins(context);

        if (result.isSuccess()) {
            log.info("Pipeline completed successfully: {}", pipelineName);
        } else {
            log.error("Pipeline failed: {}", pipelineName);
        }
        
        return result;
    }

    private PipelineResult executeStages(List<StageDefinition> stages, PipelineContext context) {
        Map<String, StageDefinition> stageMap = new HashMap<>();
        for (StageDefinition stage : stages) {
            stageMap.put(stage.getName(), stage);
        }

        Map<String, StageResult> results = new ConcurrentHashMap<>();
        Set<String> completedStages = Collections.synchronizedSet(new HashSet<>());
        Map<String, CompletableFuture<Void>> runningStages = new ConcurrentHashMap<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors())
        );

        try {
            while (completedStages.size() < stages.size()) {
                List<StageDefinition> readyStages = stages.stream()
                    .filter(s -> !completedStages.contains(s.getName()))
                    .filter(s -> {
                        List<String> deps = s.getDependsOn();
                        if (deps == null || deps.isEmpty()) {
                            return true;
                        }
                        return completedStages.containsAll(deps);
                    })
                    .toList();

                if (readyStages.isEmpty() && runningStages.isEmpty()) {
                    return PipelineResult.failed("Circular dependency detected or no runnable stages");
                }

                for (StageDefinition stage : readyStages) {
                    if (!runningStages.containsKey(stage.getName())) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> executeStage(stage, context, results),
                            executor
                        );
                        runningStages.put(stage.getName(), future);
                    }
                }

                Iterator<Map.Entry<String, CompletableFuture<Void>>> it = runningStages.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, CompletableFuture<Void>> entry = it.next();
                    try {
                        entry.getValue().get(100, TimeUnit.MILLISECONDS);
                        completedStages.add(entry.getKey());
                        it.remove();
                    } catch (TimeoutException e) {
                        // Stage still running
                    } catch (ExecutionException e) {
                        log.error("Stage execution failed: {}", entry.getKey(), e.getCause());
                        return PipelineResult.failed("Stage failed: " + entry.getKey() + " - " + e.getCause().getMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return PipelineResult.failed("Pipeline interrupted: " + e.getMessage());
                    }
                }

                if (runningStages.isEmpty() && completedStages.size() < stages.size()) {
                    return PipelineResult.failed("Deadlock or no runnable stages");
                }
            }

            for (Map.Entry<String, StageResult> entry : results.entrySet()) {
                context.addStageResult(entry.getKey(), entry.getValue());
                if (!entry.getValue().isSuccess()) {
                    return PipelineResult.failed("Stage failed: " + entry.getKey());
                }
            }

            return PipelineResult.success(context);

        } finally {
            executor.shutdown();
        }
    }

    private void executeStage(StageDefinition stage, PipelineContext context, Map<String, StageResult> results) {
        String stageName = stage.getName();
        String stageType = stage.getType();
        
        log.info("Executing stage: {} (type: {})", stageName, stageType);

        Map<String, Object> config = new HashMap<>();
        if (stage.getConfig() != null) {
            config.putAll(stage.getConfig());
        }

        int retryCount = stage.getRetry() != null ? stage.getRetry() : 0;
        StageResult finalResult = null;

        for (int attempt = 0; attempt <= retryCount; attempt++) {
            if (attempt > 0) {
                log.info("Retrying stage: {} (attempt {}/{})", stageName, attempt + 1, retryCount + 1);
            }

            StageResult result = stageExecutor.execute(stageType, config, context);
            
            if (result.isSuccess()) {
                finalResult = result;
                break;
            } else if (attempt == retryCount) {
                finalResult = result;
            }

            if (attempt < retryCount) {
                try {
                    Thread.sleep(1000 * attempt + 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finalResult = StageResult.builder()
                        .stageName(stageName)
                        .success(false)
                        .output("Stage interrupted: " + e.getMessage())
                        .durationMs(0)
                        .build();
                    throw new RuntimeException("Stage interrupted", e);
                }
            }
        }

        results.put(stageName, finalResult);
        
        if (!finalResult.isSuccess()) {
            log.error("Stage failed: {}", stageName);
        } else {
            log.info("Stage completed: {} ({}ms)", stageName, finalResult.getDurationMs());
        }
    }

    private void executePreStagePlugins(PipelineContext context) {
        var plugins = pluginManager.listStagePlugins();
        for (String pluginName : plugins) {
            StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
            if (plugin != null) {
                log.info("Running pre-stage plugin: {}", pluginName);
                plugin.execute(new HashMap<>(), new HashMap<>());
            }
        }
    }

    private void executePostStagePlugins(PipelineContext context) {
        var plugins = pluginManager.listStagePlugins();
        for (String pluginName : plugins) {
            StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
            if (plugin != null) {
                log.info("Running post-stage plugin: {}", pluginName);
                plugin.execute(new HashMap<>(), new HashMap<>());
            }
        }
    }
}
