package com.hsbc.ci.engine.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromotionPolicyTest {

    @Test
    void fastTrack_hasAutoApprove() {
        PromotionPolicy policy = PromotionPolicy.fastTrack();
        
        assertEquals("fast-track", policy.getName());
        assertTrue(policy.isAutoApprove());
        assertEquals(5, policy.getTimeoutMinutes());
    }

    @Test
    void standard_hasTestGate() {
        PromotionPolicy policy = PromotionPolicy.standard();
        
        assertEquals("standard", policy.getName());
        assertFalse(policy.isAutoApprove());
        assertNotNull(policy.getRequiredGates());
        assertTrue(policy.getRequiredGates().contains("test-passed"));
    }

    @Test
    void production_hasAllGates() {
        PromotionPolicy policy = PromotionPolicy.production();
        
        assertEquals("production", policy.getName());
        assertFalse(policy.isAutoApprove());
        assertNotNull(policy.getRequiredGates());
        assertEquals(3, policy.getRequiredGates().size());
        assertTrue(policy.getRequiredGates().contains("test-passed"));
        assertTrue(policy.getRequiredGates().contains("coverage-threshold"));
        assertTrue(policy.getRequiredGates().contains("security-scan"));
    }

    @Test
    void securityPatch_hasSecurityGate() {
        PromotionPolicy policy = PromotionPolicy.securityPatch();
        
        assertEquals("security-patch", policy.getName());
        assertFalse(policy.isAutoApprove());
        assertNotNull(policy.getRequiredGates());
        assertTrue(policy.getRequiredGates().contains("security-scan"));
    }

    @Test
    void fromType_parsesFastTrack() {
        PromotionPolicy policy = PromotionPolicy.fromType("fast-track");
        
        assertEquals("fast-track", policy.getName());
        assertTrue(policy.isAutoApprove());
    }

    @Test
    void fromType_parsesStandard() {
        PromotionPolicy policy = PromotionPolicy.fromType("standard");
        
        assertEquals("standard", policy.getName());
    }

    @Test
    void fromType_parsesProduction() {
        PromotionPolicy policy = PromotionPolicy.fromType("production");
        
        assertEquals("production", policy.getName());
    }

    @Test
    void fromType_parsesSecurityPatch() {
        PromotionPolicy policy = PromotionPolicy.fromType("security-patch");
        
        assertEquals("security-patch", policy.getName());
    }

    @Test
    void fromType_handlesUnknownType() {
        PromotionPolicy policy = PromotionPolicy.fromType("custom");
        
        assertEquals("custom", policy.getName());
        assertEquals("CUSTOM", policy.getType());
    }

    @Test
    void allPoliciesAllowRollback() {
        assertTrue(PromotionPolicy.fastTrack().isAllowRollback());
        assertTrue(PromotionPolicy.standard().isAllowRollback());
        assertTrue(PromotionPolicy.production().isAllowRollback());
        assertTrue(PromotionPolicy.securityPatch().isAllowRollback());
    }

    @Test
    void settersAndGettersWork() {
        PromotionPolicy policy = new PromotionPolicy();
        
        policy.setName("test-policy");
        policy.setDescription("Test description");
        policy.setType("TEST");
        policy.setRequiredGates(List.of("gate1", "gate2"));
        policy.setApprovers(List.of("user1"));
        policy.setAutoApprove(true);
        policy.setTimeoutMinutes(30);
        policy.setAllowRollback(false);
        
        assertEquals("test-policy", policy.getName());
        assertEquals("Test description", policy.getDescription());
        assertEquals("TEST", policy.getType());
        assertEquals(2, policy.getRequiredGates().size());
        assertEquals(1, policy.getApprovers().size());
        assertTrue(policy.isAutoApprove());
        assertEquals(30, policy.getTimeoutMinutes());
        assertFalse(policy.isAllowRollback());
    }
}
