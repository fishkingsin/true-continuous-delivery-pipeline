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
}
