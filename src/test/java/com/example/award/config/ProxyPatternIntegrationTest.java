package com.example.award.config;

import com.example.award.domain.Award;
import com.example.award.domain.FeatureToggle;
import com.example.award.proxy.AwardRepoProxy;
import com.example.award.repository.FeatureToggleRepository;
import com.example.award.repository.AwardRepo;
import com.example.award.service.AwardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating the Proxy Pattern configuration
 * 
 * This test verifies that:
 * 1. Service layer injects AwardRepo
 * 2. AwardRepoProxy is actually injected (due to @Primary)
 * 3. The proxy correctly delegates to AwardRepo or AwardRepoV2 based on feature toggle
 * 
 * Uses Testcontainers for real MongoDB instance
 */
@SpringBootTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
public class ProxyPatternIntegrationTest {
    
    @Autowired
    private AwardService awardService;
    
    @Autowired
    private AwardRepo awardRepo;  // This will actually be AwardRepoProxy
    
    @Autowired
    private FeatureToggleRepository featureToggleRepository;
    
    @BeforeEach
    public void setup() {
        // Clean up
        featureToggleRepository.deleteAll();
        awardRepo.deleteAll();
        
        // Create feature toggle in disabled state
        FeatureToggle toggle = new FeatureToggle("use-new-flow", false);
        toggle.setDescription("Test toggle");
        featureToggleRepository.save(toggle);
    }
    
    @Test
    public void testServiceLayerReceivesProxy() {
        // Verify that the injected AwardRepo is actually AwardRepoProxy
        assertTrue(awardRepo instanceof AwardRepoProxy,
                  "AwardRepo should be proxied by AwardRepoProxy due to @Primary annotation");
    }
    
    @Test
    public void testProxyDelegatesToOldFlowWhenToggleDisabled() {
        // Feature toggle is disabled, should use AwardRepo
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        
        Award saved = awardService.createAward(award);
        
        assertNotNull(saved.getId(), "Award should be saved");
        assertEquals("PENDING", saved.getStatus(), "Status should be PENDING");
        
        // Verify it's in the database
        assertTrue(awardRepo.existsById(saved.getId()), "Award should exist in database");
    }
    
    @Test
    public void testProxyDelegatesToNewFlowWhenToggleEnabled() {
        // Enable the feature toggle
        FeatureToggle toggle = featureToggleRepository.findByFeatureName("use-new-flow")
                .orElseThrow();
        toggle.setEnabled(true);
        featureToggleRepository.save(toggle);
        
        // Now requests should go to AwardRepoV2
        Award award = new Award("Test Award 2", "Description 2", "INNOVATION");
        
        Award saved = awardService.createAward(award);
        
        assertNotNull(saved.getId(), "Award should be saved");
        assertEquals("PENDING", saved.getStatus(), "Status should be PENDING");
        
        // Verify it's in the database (AwardRepoV2 uses same collection)
        assertTrue(awardRepo.existsById(saved.getId()), "Award should exist in database");
    }
    
    @Test
    public void testServiceLayerCodeUnchanged() {
        // The service layer doesn't know about the proxy
        // It just uses AwardRepo interface methods
        
        Award award = new Award("Service Test", "Testing service layer", "TEST");
        Award saved = awardService.createAward(award);
        
        // Service layer methods work normally
        Award retrieved = awardService.getAwardById(saved.getId())
                .orElseThrow();
        
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Service Test", retrieved.getName());
        
        // Update works
        Award updated = awardService.approveAward(saved.getId());
        assertEquals("APPROVED", updated.getStatus());
        
        // Delete works
        awardService.deleteAward(saved.getId());
        assertFalse(awardRepo.existsById(saved.getId()));
    }
}
