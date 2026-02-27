package com.hsbc.ci.engine.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.Map;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigurationLoaderTest {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Test
    void loadYamlFile_withValidYaml_returnsMap() {
        Map<String, Object> result = configurationLoader.loadYamlFile("config/checkout.yml");
        
        assertNotNull(result);
        assertTrue(result.containsKey("checkout"));
    }

    @Test
    void loadYamlFile_withNonExistentFile_throwsException() {
        assertThrows(RuntimeException.class, () -> {
            configurationLoader.loadYamlFile("config/nonexistent.yml");
        });
    }

    @Test
    void getPipeline_withExistingPipeline_returnsConfig() {
        Map<String, Object> pipeline = configurationLoader.getPipeline("sample-pipeline");
        
        assertNotNull(pipeline);
        assertEquals("sample-pipeline", pipeline.get("name"));
    }

    @Test
    void getPipeline_withNonExistentPipeline_returnsNull() {
        Map<String, Object> pipeline = configurationLoader.getPipeline("nonexistent");
        
        assertNull(pipeline);
    }

    @Test
    void listPipelines_returnsAllPipelineNames() {
        Collection<String> pipelines = configurationLoader.listPipelines();
        
        assertNotNull(pipelines);
        assertFalse(pipelines.isEmpty());
        assertTrue(pipelines.contains("sample-pipeline"));
    }

    @Test
    void loadYamlFile_checkoutConfig_hasRepositories() {
        Map<String, Object> result = configurationLoader.loadYamlFile("config/checkout.yml");
        
        assertNotNull(result);
        Map<String, Object> checkout = (Map<String, Object>) result.get("checkout");
        assertNotNull(checkout);
        
        var repositories = (java.util.List<Map<String, Object>>) checkout.get("repositories");
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty());
        
        Map<String, Object> firstRepo = repositories.get(0);
        assertEquals("myapp", firstRepo.get("name"));
        assertTrue(((String) firstRepo.get("url")).contains("github.com"));
    }

    @Test
    void loadYamlFile_buildConfig_hasProjects() {
        Map<String, Object> result = configurationLoader.loadYamlFile("config/build.yml");
        
        assertNotNull(result);
        Map<String, Object> build = (Map<String, Object>) result.get("build");
        assertNotNull(build);
        
        var projects = (java.util.List<Map<String, Object>>) build.get("projects");
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
    }

    @Test
    void loadYamlFile_deployConfig_hasTargets() {
        Map<String, Object> result = configurationLoader.loadYamlFile("config/deploy.yml");
        
        assertNotNull(result);
        assertTrue(result.containsKey("deploy"));
    }

    @Test
    void loadYamlFile_environmentsConfig_hasEnvironments() {
        Map<String, Object> result = configurationLoader.loadYamlFile("config/environments.yml");
        
        assertNotNull(result);
        assertTrue(result.containsKey("environments"));
    }
}
