package com.example.award.facade;

import com.example.award.config.TestContainersConfiguration;
import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FeatureToggleService to verify YAML + DB behaviour
 * in a real Spring context.
 *
 * Ensures migration confidence: feature flags loaded from application-test.yml
 * and DB fallback work correctly together.
 */
@SpringBootTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class FeatureToggleServiceIntegrationTest {

    @Autowired
    private FeatureToggleService featureToggleService;

    @Autowired
    private FeatureToggleRepository featureToggleRepository;

    @BeforeEach
    void setUp() {
        featureToggleRepository.deleteAll();
    }

    /**
     * Verifies that a flag defined only in YAML (application-test.yml) is read correctly
     * without any database interaction. Proves YAML configuration is loaded and used.
     */
    @Test
    void isFeatureEnabled_shouldReturnYamlValueWhenOnlyInYaml() {
        // application-test.yml defines: yaml-only-feature: true
        // No DB entry for this feature

        boolean enabled = featureToggleService.isFeatureEnabled("yaml-only-feature");

        assertTrue(enabled, "Flag defined in YAML only should return true from application-test.yml");
    }

    /**
     * Verifies YAML takes precedence when the same flag exists in both YAML and DB
     * with different values. Critical for migration: config file must override runtime DB.
     */
    @Test
    void isFeatureEnabled_shouldPreferYamlOverDbWhenBothExist() {
        // application-test.yml defines: use-new-flow: false
        // Create DB entry with opposite value
        FeatureToggle dbToggle = new FeatureToggle("use-new-flow", true);
        featureToggleRepository.save(dbToggle);

        boolean enabled = featureToggleService.isFeatureEnabled("use-new-flow");

        assertFalse(enabled,
                "YAML value must take precedence over DB when both define the same flag");
    }

    /**
     * Verifies that when a flag is not in YAML but DB fallback is enabled,
     * the value from the database is returned.
     */
    @Test
    void isFeatureEnabled_shouldReturnDbValueWhenNotInYamlAndFallbackEnabled() {
        FeatureToggle dbToggle = new FeatureToggle("db-only-feature", true);
        featureToggleRepository.save(dbToggle);

        boolean enabled = featureToggleService.isFeatureEnabled("db-only-feature");

        assertTrue(enabled, "When not in YAML and fallback enabled, DB value should be used");
    }

    /**
     * Verifies that when the flag is not in YAML and not in DB,
     * the service returns false (safe default).
     */
    @Test
    void isFeatureEnabled_shouldReturnFalseWhenNotFoundInYamlOrDb() {
        boolean enabled = featureToggleService.isFeatureEnabled("non-existent-feature");

        assertFalse(enabled, "Unknown flag must default to false");
    }
}
