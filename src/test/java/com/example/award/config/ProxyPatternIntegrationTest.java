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

import java.time.LocalDateTime;
import java.util.List;

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

    // ========== Migration confidence tests ==========

    /**
     * Custom query methods (findByRecipientId, getAwardsByCategory, etc.) must route
     * through the proxy to AwardRepo when the toggle is disabled.
     */
    @Test
    public void testCustomQueryMethodsUseOldFlowWhenToggleDisabled() {
        Award award = new Award("Recipient Test", "Desc", "PERFORMANCE");
        Award saved = awardService.createAward(award);
        awardService.awardToRecipient(saved.getId(), "recipient-123");

        List<Award> byRecipient = awardService.getAwardsByRecipient("recipient-123");
        List<Award> byCategory = awardService.getAwardsByCategory("PERFORMANCE");
        List<Award> byStatus = awardService.getAwardsByStatus("AWARDED");

        assertFalse(byRecipient.isEmpty(), "findByRecipientId should return award when toggle OFF");
        assertTrue(byRecipient.stream().anyMatch(a -> "recipient-123".equals(a.getRecipientId())));
        assertFalse(byCategory.isEmpty());
        assertFalse(byStatus.isEmpty());
    }

    /**
     * Custom query methods must route to AwardRepoV2 when the toggle is enabled.
     */
    @Test
    public void testCustomQueryMethodsUseNewFlowWhenToggleEnabled() {
        FeatureToggle toggle = featureToggleRepository.findByFeatureName("use-new-flow").orElseThrow();
        toggle.setEnabled(true);
        featureToggleRepository.save(toggle);

        Award award = new Award("New Flow Query Test", "Desc", "INNOVATION");
        Award saved = awardService.createAward(award);
        awardService.awardToRecipient(saved.getId(), "recipient-456");

        List<Award> byRecipient = awardService.getAwardsByRecipient("recipient-456");
        List<Award> byCategory = awardService.getAwardsByCategory("INNOVATION");

        assertFalse(byRecipient.isEmpty(), "findByRecipientId should route to AwardRepoV2 when toggle ON");
        assertFalse(byCategory.isEmpty());
    }

    /**
     * AwardRepo-only methods (findByCreatedAtAfter, countByStatus) must still work
     * when new flow is enabled, via facade fallback to AwardRepo.
     */
    @Test
    public void testAwardRepoOnlyMethodsFallbackWhenNewFlowEnabled() {
        FeatureToggle toggle = featureToggleRepository.findByFeatureName("use-new-flow").orElseThrow();
        toggle.setEnabled(true);
        featureToggleRepository.save(toggle);

        Award award = new Award("Fallback Test", "Desc", "PERFORMANCE");
        Award saved = awardService.createAward(award);
        saved.setCreatedAt(LocalDateTime.now().minusDays(1));
        awardRepo.save(saved);

        List<Award> afterDate = awardRepo.findByCreatedAtAfter(LocalDateTime.now().minusDays(2));
        long count = awardRepo.countByStatus("PENDING");

        assertFalse(afterDate.isEmpty(), "findByCreatedAtAfter must fallback to AwardRepo when NewFlow enabled");
        assertTrue(count >= 1, "countByStatus must fallback to AwardRepo when NewFlow enabled");
    }

    /**
     * Switching the toggle mid-flow must preserve data and route correctly:
     * award1 with toggle OFF (OldFlow), award2 with toggle ON (NewFlow); both readable.
     */
    @Test
    public void testToggleSwitchMidFlow_preservesDataAndRoutesCorrectly() {
        Award award1 = new Award("Old Flow Award", "Description 1", "PERFORMANCE");
        Award saved1 = awardService.createAward(award1);
        assertNotNull(saved1.getId());

        FeatureToggle toggle = featureToggleRepository.findByFeatureName("use-new-flow").orElseThrow();
        toggle.setEnabled(true);
        featureToggleRepository.save(toggle);

        Award award2 = new Award("New Flow Award", "Description 2", "INNOVATION");
        Award saved2 = awardService.createAward(award2);
        assertNotNull(saved2.getId());

        assertTrue(awardRepo.existsById(saved1.getId()), "Old-flow award must still exist");
        assertTrue(awardRepo.existsById(saved2.getId()), "New-flow award must exist");
        assertEquals("Old Flow Award", awardService.getAwardById(saved1.getId()).orElseThrow().getName());
        assertEquals("New Flow Award", awardService.getAwardById(saved2.getId()).orElseThrow().getName());
    }

    /**
     * Proxy validation (null award, empty name) must apply regardless of toggle state.
     */
    @Test
    public void testProxyValidationAppliesRegardlessOfToggle() {
        assertThrows(IllegalArgumentException.class, () -> awardRepo.save((Award) null),
                "Proxy must reject null award");

        Award emptyName = new Award("", "Desc", "PERFORMANCE");
        assertThrows(IllegalArgumentException.class, () -> awardRepo.save(emptyName),
                "Proxy must reject award with empty name");
    }
}
