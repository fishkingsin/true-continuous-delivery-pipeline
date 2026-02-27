package com.hsbc.ci.engine.core.config;

import com.hsbc.ci.engine.core.model.Environment;
import com.hsbc.ci.engine.core.model.PipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentLoaderTest {

    private EnvironmentLoader environmentLoader;

    @BeforeEach
    void setUp() {
        environmentLoader = new EnvironmentLoader();
    }

    @Test
    void getEnvironment_returnsEnvironmentByName() {
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        deploy.setType("kubernetes");
        deploy.setNamespace("dev");
        deploy.setCluster("dev-cluster");

        Environment env = new Environment();
        env.setName("dev");
        env.setDescription("Development environment");
        env.setOrder(1);
        env.setDeploy(deploy);

        environmentLoader.getEnvironments().put("dev", env);

        Environment result = environmentLoader.getEnvironment("dev");

        assertNotNull(result);
        assertEquals("dev", result.getName());
        assertEquals("Development environment", result.getDescription());
        assertEquals(1, result.getOrder());
    }

    @Test
    void getEnvironment_returnsNullForUnknown() {
        Environment result = environmentLoader.getEnvironment("unknown");
        assertNull(result);
    }

    @Test
    void listEnvironments_returnsAllEnvironments() {
        Environment dev = new Environment();
        dev.setName("dev");
        dev.setOrder(1);

        Environment staging = new Environment();
        staging.setName("staging");
        staging.setOrder(2);

        environmentLoader.getEnvironments().put("dev", dev);
        environmentLoader.getEnvironments().put("staging", staging);

        Collection<Environment> result = environmentLoader.listEnvironments();

        assertEquals(2, result.size());
    }

    @Test
    void getOrderedEnvironments_returnsSortedByOrder() {
        Environment dev = new Environment();
        dev.setName("dev");
        dev.setOrder(3);

        Environment staging = new Environment();
        staging.setName("staging");
        staging.setOrder(1);

        Environment prod = new Environment();
        prod.setName("prod");
        prod.setOrder(2);

        environmentLoader.getEnvironments().put("dev", dev);
        environmentLoader.getEnvironments().put("staging", staging);
        environmentLoader.getEnvironments().put("prod", prod);

        List<Environment> result = environmentLoader.getOrderedEnvironments();

        assertEquals("staging", result.get(0).getName());
        assertEquals("prod", result.get(1).getName());
        assertEquals("dev", result.get(2).getName());
    }

    @Test
    void getEnvironmentVariables_returnsVarsForEnvironment() {
        Environment env = new Environment();
        env.setName("dev");
        
        Environment.Resources resources = new Environment.Resources();
        resources.setCpu("2");
        resources.setMemory("4Gi");
        env.setResources(resources);
        
        env.setReplicas(3);

        environmentLoader.getEnvironments().put("dev", env);

        Map<String, String> vars = environmentLoader.getEnvironmentVariables("dev");

        assertEquals("2", vars.get("CPU"));
        assertEquals("4Gi", vars.get("MEMORY"));
        assertEquals("3", vars.get("REPLICAS"));
    }

    @Test
    void validate_returnsTrueForValidEnvironment() {
        Environment env = new Environment();
        env.setName("dev");
        
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        deploy.setType("kubernetes");
        env.setDeploy(deploy);

        environmentLoader.getEnvironments().put("dev", env);

        boolean result = environmentLoader.validate("dev");

        assertTrue(result);
    }

    @Test
    void validate_returnsFalseForUnknownEnvironment() {
        boolean result = environmentLoader.validate("unknown");
        assertFalse(result);
    }

    @Test
    void validate_returnsFalseForMissingName() {
        Environment env = new Environment();
        env.setName("");

        environmentLoader.getEnvironments().put("empty", env);

        boolean result = environmentLoader.validate("empty");

        assertFalse(result);
    }

    @Test
    void validate_returnsFalseForMissingDeployType() {
        Environment env = new Environment();
        env.setName("dev");
        
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        deploy.setType(null);
        env.setDeploy(deploy);

        environmentLoader.getEnvironments().put("dev", env);

        boolean result = environmentLoader.validate("dev");

        assertFalse(result);
    }

    @Test
    void applyEnvironmentOverrides_addsVarsToContext() {
        Environment env = new Environment();
        env.setName("dev");
        env.setOrder(1);
        
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        deploy.setType("kubernetes");
        deploy.setNamespace("dev-ns");
        deploy.setCluster("dev-cluster");
        deploy.setStrategy("RollingUpdate");
        env.setDeploy(deploy);

        environmentLoader.getEnvironments().put("dev", env);

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .environment("dev")
            .build();

        environmentLoader.applyEnvironmentOverrides(context);

        assertEquals("dev", context.getVariable("ENVIRONMENT_NAME"));
        assertEquals("1", context.getVariable("ENVIRONMENT_ORDER"));
        assertEquals("kubernetes", context.getVariable("DEPLOY_TYPE"));
        assertEquals("dev-ns", context.getVariable("DEPLOY_NAMESPACE"));
        assertEquals("dev-cluster", context.getVariable("DEPLOY_CLUSTER"));
        assertEquals("RollingUpdate", context.getVariable("DEPLOY_STRATEGY"));
    }

    @Test
    void applyEnvironmentOverrides_doesNothingForNullEnvironment() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .environment(null)
            .build();

        environmentLoader.applyEnvironmentOverrides(context);

        assertNull(context.getVariable("ENVIRONMENT_NAME"));
    }

    @Test
    void getNextEnvironment_returnsNextInOrder() {
        Environment dev = new Environment();
        dev.setName("dev");
        dev.setOrder(1);

        Environment staging = new Environment();
        staging.setName("staging");
        staging.setOrder(2);

        Environment prod = new Environment();
        prod.setName("prod");
        prod.setOrder(3);

        environmentLoader.getEnvironments().put("dev", dev);
        environmentLoader.getEnvironments().put("staging", staging);
        environmentLoader.getEnvironments().put("prod", prod);

        Environment next = environmentLoader.getNextEnvironment("staging");

        assertNotNull(next);
        assertEquals("prod", next.getName());
    }

    @Test
    void getNextEnvironment_returnsNullForLastEnvironment() {
        Environment prod = new Environment();
        prod.setName("prod");
        prod.setOrder(1);

        environmentLoader.getEnvironments().put("prod", prod);

        Environment next = environmentLoader.getNextEnvironment("prod");

        assertNull(next);
    }

    @Test
    void shouldAutoPromote_returnsTrueWhenEnabled() {
        Environment env = new Environment();
        env.setName("dev");
        env.setAutoPromote(true);

        environmentLoader.getEnvironments().put("dev", env);

        boolean result = environmentLoader.shouldAutoPromote("dev");

        assertTrue(result);
    }

    @Test
    void shouldAutoPromote_returnsFalseWhenDisabled() {
        Environment env = new Environment();
        env.setName("dev");
        env.setAutoPromote(false);

        environmentLoader.getEnvironments().put("dev", env);

        boolean result = environmentLoader.shouldAutoPromote("dev");

        assertFalse(result);
    }

    @Test
    void shouldAutoPromote_returnsFalseForUnknown() {
        boolean result = environmentLoader.shouldAutoPromote("unknown");
        assertFalse(result);
    }

    @Test
    void getPromotionChain_returnsChainFromEnvironment() {
        Environment dev = new Environment();
        dev.setName("dev");
        dev.setOrder(1);

        Environment staging = new Environment();
        staging.setName("staging");
        staging.setOrder(2);

        Environment prod = new Environment();
        prod.setName("prod");
        prod.setOrder(3);

        environmentLoader.getEnvironments().put("dev", dev);
        environmentLoader.getEnvironments().put("staging", staging);
        environmentLoader.getEnvironments().put("prod", prod);

        List<Environment> chain = environmentLoader.getPromotionChain("staging");

        assertEquals(2, chain.size());
        assertEquals("staging", chain.get(0).getName());
        assertEquals("prod", chain.get(1).getName());
    }

    @Test
    void getPromotionChain_returnsEmptyForUnknown() {
        List<Environment> chain = environmentLoader.getPromotionChain("unknown");
        assertTrue(chain.isEmpty());
    }
}
