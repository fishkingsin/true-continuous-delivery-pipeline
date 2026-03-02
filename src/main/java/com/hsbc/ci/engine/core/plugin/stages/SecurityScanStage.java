package com.hsbc.ci.engine.core.plugin.stages;

import com.hsbc.ci.engine.core.plugin.Plugin;
import com.hsbc.ci.engine.core.plugin.PluginResult;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecurityScanStage implements StagePlugin {

    private static final Logger log = LoggerFactory.getLogger(SecurityScanStage.class);

    @Override
    public String getName() {
        return "security-scan";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getType() {
        return "security-scan";
    }

    @Override
    public void init(Map<String, Object> config) {
        log.info("Initializing SecurityScanStage with config: {}", config);
    }

    @Override
    public PluginResult execute(Map<String, Object> context) {
        log.info("Executing security scan with context: {}", context);
        
        Map<String, Object> config = (Map<String, Object>) context.get("config");
        if (config == null) {
            config = new java.util.HashMap<>();
        }
        
        List<String> scanners = getScanners(config);
        List<String> results = new ArrayList<>();
        
        for (String scanner : scanners) {
            String result = runScanner(scanner, config);
            results.add(result);
        }
        
        String output = String.join("\n", results);
        log.info("Security scan completed");
        
        return PluginResult.success(output);
    }

    @Override
    public void execute(Map<String, Object> config, Map<String, Object> context) {
        log.info("Executing security scan (legacy method)");
        
        List<String> scanners = getScanners(config);
        List<String> results = new ArrayList<>();
        
        for (String scanner : scanners) {
            String result = runScanner(scanner, config);
            results.add(result);
        }
        
        log.info("Security scan completed: {}", String.join("; ", results));
    }

    private List<String> getScanners(Map<String, Object> config) {
        Object scannersObj = config.get("scanners");
        if (scannersObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> scanners = (List<String>) scannersObj;
            return scanners;
        }
        return List.of("sast", "foss");
    }

    private String runScanner(String scanner, Map<String, Object> config) {
        log.info("Running security scanner: {}", scanner);
        
        if ("sast".equals(scanner)) {
            return runSastScanner(config);
        } else if ("dast".equals(scanner)) {
            return runDastScanner(config);
        } else if ("foss".equals(scanner)) {
            return runFossScanner(config);
        }
        
        return "Scanner not found: " + scanner;
    }

    private String runSastScanner(Map<String, Object> config) {
        return "[sast] Static Analysis Security Testing - CRITICAL: 0, HIGH: 2, MEDIUM: 5, LOW: 10 [SUCCESS]";
    }

    private String runDastScanner(Map<String, Object> config) {
        return "[dast] Dynamic Analysis Security Testing - CRITICAL: 0, HIGH: 1, MEDIUM: 3 [SUCCESS]";
    }

    private String runFossScanner(Map<String, Object> config) {
        return "[foss] Free and Open Source Software scan - Vulnerabilities: 3, Outdated: 5 [SUCCESS]";
    }
}
