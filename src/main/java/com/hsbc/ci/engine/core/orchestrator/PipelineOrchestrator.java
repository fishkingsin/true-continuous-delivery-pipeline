package com.hsbc.ci.engine.core.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.config.EnvironmentLoader;
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
    private static final int POLLING_INTERVAL_MS = 100;
    private static final int MIN_THREAD_POOL_SIZE = 2;
    private static final int THREAD_MULTIPLIER = 1;

    private final ConfigurationLoader configLoader;
    private final StageExecutor stageExecutor;
    private final PluginManager pluginManager;
    private final PipelineValidator pipelineValidator;
    private final EnvironmentLoader environmentLoader;

    public PipelineOrchestrator() {
        this(null, null, null, null, null);
    }

    @Autowired
    public PipelineOrchestrator(ConfigurationLoader configLoader, 
                                StageExecutor stageExecutor,
                                PluginManager pluginManager,
                                PipelineValidator pipelineValidator,
                                EnvironmentLoader environmentLoader) {
        this.configLoader = configLoader;
        this.stageExecutor = stageExecutor;
        this.pluginManager = pluginManager;
        this.pipelineValidator = pipelineValidator != null ? pipelineValidator : new PipelineValidator();
        this.environmentLoader = environmentLoader;
    }

    public PipelineResult execute(PipelineContext context) {
        PipelineDefinition pipelineDef = loadPipeline(context.getPipelineName());
        if (pipelineDef == null) return failed("Pipeline not found");

        if (!validatePipeline(pipelineDef)) return failed("Validation failed");

        applyEnvironmentOverrides(context);

        if (context.isDryRun()) return dryRunResult(context);

        return executePipeline(context, pipelineDef);
    }

    private PipelineDefinition loadPipeline(String name) {
        return configLoader != null ? configLoader.loadPipelineDefinition(name) : null;
    }

    private boolean validatePipeline(PipelineDefinition def) {
        var result = pipelineValidator.validate(def);
        if (!result.isValid()) {
            log.error("Validation failed: {}", result.getErrors());
            return false;
        }
        return true;
    }

    private PipelineResult failed(String msg) {
        return PipelineResult.failed(msg);
    }

    private PipelineResult dryRunResult(PipelineContext ctx) {
        log.info("DRY-RUN mode");
        return PipelineResult.success(ctx);
    }

    private PipelineResult executePipeline(PipelineContext ctx, PipelineDefinition def) {
        log.info("Executing: {}", ctx.getPipelineName());
        executePreStagePlugins(ctx);
        
        var result = runStages(def.getStages(), ctx);
        
        executePostStagePlugins(ctx);
        return result;
    }

    private PipelineResult runStages(List<StageDefinition> stages, PipelineContext ctx) {
        var results = new ConcurrentHashMap<String, StageResult>();
        var completed = Collections.synchronizedSet(new HashSet<String>());
        var running = new ConcurrentHashMap<String, CompletableFuture<Void>>();
        ExecutorService executor = createExecutor();

        try {
            while (completed.size() < stages.size()) {
                var ready = findReadyStages(stages, completed);
                if (ready.isEmpty() && running.isEmpty()) return PipelineResult.failed("No runnable stages");

                submitStages(ready, executor, ctx, results, running);
                collectCompletedStages(running, completed);
            }
            return buildResult(ctx, results);
        } finally {
            executor.shutdown();
        }
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(
            Math.max(MIN_THREAD_POOL_SIZE, 
                Runtime.getRuntime().availableProcessors() * THREAD_MULTIPLIER));
    }

    private List<StageDefinition> findReadyStages(List<StageDefinition> all, Set<String> done) {
        return all.stream()
            .filter(s -> !done.contains(s.getName()))
            .filter(s -> depsMet(s, done))
            .collect(Collectors.toList());
    }

    private boolean depsMet(StageDefinition s, Set<String> done) {
        var deps = s.getDependsOn();
        return deps == null || deps.isEmpty() || done.containsAll(deps);
    }

    private void submitStages(List<StageDefinition> ready, ExecutorService exec, 
            PipelineContext ctx, Map<String,StageResult> results, Map<String,CompletableFuture<Void>> running) {
        for (var stage : ready) {
            if (!running.containsKey(stage.getName())) {
                var future = CompletableFuture.runAsync(
                    () -> runSingleStage(stage, ctx, results), exec);
                running.put(stage.getName(), future);
            }
        }
    }

    private void collectCompletedStages(Map<String,CompletableFuture<Void>> running, Set<String> done) {
        var it = running.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            try {
                entry.getValue().get(POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
                done.add(entry.getKey());
                it.remove();
            } catch (TimeoutException e) {
                // still running
            } catch (ExecutionException e) {
                log.error("Stage failed: {}", entry.getKey());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private PipelineResult buildResult(PipelineContext ctx, Map<String,StageResult> results) {
        for (var entry : results.entrySet()) {
            ctx.addStageResult(entry.getKey(), entry.getValue());
            if (!entry.getValue().isSuccess()) {
                return PipelineResult.failed("Stage failed: " + entry.getKey());
            }
        }
        return PipelineResult.success(ctx);
    }

    private void runSingleStage(StageDefinition stage, PipelineContext ctx, Map<String,StageResult> results) {
        String name = stage.getName();
        String type = stage.getType();
        log.info("Running stage: {} ({})", name, type);

        var config = new HashMap<String,Object>();
        if (stage.getConfig() != null) config.putAll(stage.getConfig());

        int retries = stage.getRetry() != null ? stage.getRetry() : 0;
        StageResult result = null;

        for (int i = 0; i <= retries; i++) {
            result = stageExecutor.execute(type, config, ctx);
            if (result.isSuccess() || i == retries) break;
            log.info("Retrying {} (attempt {}/{})", name, i + 1, retries);
            sleep(i * 1000);
        }

        results.put(name, result);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void executePreStagePlugins(PipelineContext ctx) {
        runPlugins("pre-stage");
    }

    private void applyEnvironmentOverrides(PipelineContext ctx) {
        if (environmentLoader != null) {
            environmentLoader.applyEnvironmentOverrides(ctx);
        }
    }

    private void executePostStagePlugins(PipelineContext ctx) {
        runPlugins("post-stage");
    }

    private void runPlugins(String phase) {
        for (String name : pluginManager.listStagePlugins()) {
            var plugin = pluginManager.getStagePlugin(name);
            if (plugin != null) {
                log.info("Running {} plugin: {}", phase, name);
                plugin.execute(new HashMap<>(), new HashMap<>());
            }
        }
    }
}
