package com.hsbc.ci.engine.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void environment_hasGettersAndSetters() {
        Environment env = new Environment();
        
        env.setName("dev");
        env.setDescription("Development environment");
        env.setOrder(1);
        env.setAutoPromote(true);
        env.setReplicas(3);
        env.setBackup(true);
        
        assertEquals("dev", env.getName());
        assertEquals("Development environment", env.getDescription());
        assertEquals(1, env.getOrder());
        assertTrue(env.getAutoPromote());
        assertEquals(3, env.getReplicas());
        assertTrue(env.getBackup());
    }

    @Test
    void environment_hasNestedClasses() {
        Environment env = new Environment();
        
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        deploy.setType("kubernetes");
        deploy.setNamespace("dev-ns");
        deploy.setCluster("dev-cluster");
        deploy.setStrategy("RollingUpdate");
        env.setDeploy(deploy);
        
        Environment.Approval approval = new Environment.Approval();
        approval.setType("manual");
        approval.setRoles(List.of("lead", "manager"));
        approval.setTimeout("60m");
        env.setApproval(approval);
        
        Environment.Resources resources = new Environment.Resources();
        resources.setCpu("2");
        resources.setMemory("4Gi");
        env.setResources(resources);
        
        assertEquals("kubernetes", env.getDeploy().getType());
        assertEquals("dev-ns", env.getDeploy().getNamespace());
        assertEquals("dev-cluster", env.getDeploy().getCluster());
        assertEquals("RollingUpdate", env.getDeploy().getStrategy());
        
        assertEquals("manual", env.getApproval().getType());
        assertEquals(2, env.getApproval().getRoles().size());
        assertEquals("60m", env.getApproval().getTimeout());
        
        assertEquals("2", env.getResources().getCpu());
        assertEquals("4Gi", env.getResources().getMemory());
    }

    @Test
    void environment_supportsGates() {
        Environment env = new Environment();
        env.setGates(List.of("test-passed", "coverage-threshold"));
        
        assertEquals(2, env.getGates().size());
        assertTrue(env.getGates().contains("test-passed"));
    }

    @Test
    void environment_supportsMonitoring() {
        Environment env = new Environment();
        Map<String, Boolean> monitoring = Map.of(
            "prometheus", true,
            "datadog", false
        );
        env.setMonitoring(monitoring);
        
        assertTrue(env.getMonitoring().get("prometheus"));
        assertFalse(env.getMonitoring().get("datadog"));
    }

    @Test
    void environment_defaultValues() {
        Environment env = new Environment();
        
        assertNull(env.getName());
        assertNull(env.getDescription());
        assertNull(env.getOrder());
        assertNull(env.getAutoPromote());
        assertNull(env.getDeploy());
        assertNull(env.getApproval());
    }

    @Test
    void deployConfig_defaultValues() {
        Environment.DeployConfig deploy = new Environment.DeployConfig();
        
        assertNull(deploy.getType());
        assertNull(deploy.getNamespace());
        assertNull(deploy.getCluster());
        assertNull(deploy.getStrategy());
    }

    @Test
    void approval_defaultValues() {
        Environment.Approval approval = new Environment.Approval();
        
        assertNull(approval.getType());
        assertNull(approval.getRoles());
        assertNull(approval.getTimeout());
    }

    @Test
    void resources_defaultValues() {
        Environment.Resources resources = new Environment.Resources();
        
        assertNull(resources.getCpu());
        assertNull(resources.getMemory());
    }
}
