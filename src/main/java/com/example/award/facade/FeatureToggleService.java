package com.example.award.facade;

import com.example.award.config.FeatureToggleProperties;
import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing feature toggles.
 * 
 * Priority order:
 * 1. Check application.yml first (simpler, cleaner)
 * 2. Fallback to database if enabled and not found in YAML
 * 3. Default to false if not found anywhere
 * 
 * This provides a clean, simple way to manage feature flags:
 * - Simple flags: Define in application.yml (no DB needed)
 * - Dynamic flags: Use DB fallback for runtime changes
 */
@Service
public class FeatureToggleService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureToggleService.class);
    
    private final FeatureToggleRepository featureToggleRepository;
    private final FeatureToggleProperties properties;
    
    @Autowired
    public FeatureToggleService(
            FeatureToggleRepository featureToggleRepository,
            FeatureToggleProperties properties) {
        this.featureToggleRepository = featureToggleRepository;
        this.properties = properties;
    }
    
    /**
     * Check if a feature is enabled.
     * 
     * Priority:
     * 1. application.yml (if defined)
     * 2. Database (if db-fallback-enabled: true and not in YAML)
     * 3. false (default)
     * 
     * @param featureName Feature name to check
     * @return true if enabled, false otherwise
     */
    public boolean isFeatureEnabled(String featureName) {
        // Step 1: Check YAML first (simpler, cleaner)
        if (properties.hasFlag(featureName)) {
            Boolean yamlValue = properties.getFlag(featureName);
            logger.debug("Feature '{}' found in YAML: {}", featureName, yamlValue);
            return yamlValue != null && yamlValue;
        }
        
        // Step 2: Check database if fallback is enabled
        if (properties.isDbFallbackEnabled()) {
            Optional<FeatureToggle> dbToggle = featureToggleRepository.findByFeatureName(featureName);
            if (dbToggle.isPresent()) {
                boolean enabled = dbToggle.get().isEnabled();
                logger.debug("Feature '{}' found in DB: {}", featureName, enabled);
                return enabled;
            }
        }
        
        // Step 3: Default to false
        logger.debug("Feature '{}' not found, defaulting to disabled", featureName);
        return false;
    }
    
    /**
     * Enable a feature (saves to database).
     * Note: YAML flags are read-only. Use this method for runtime changes.
     * If feature exists in YAML, it will override DB value when reading.
     */
    public FeatureToggle enableFeature(String featureName, String description) {
        // Warn if feature is defined in YAML (YAML takes precedence)
        if (properties.hasFlag(featureName)) {
            logger.warn("Feature '{}' is defined in YAML. YAML value will override DB value when reading.", featureName);
        }
        
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        FeatureToggle toggle;
        if (existingToggle.isPresent()) {
            toggle = existingToggle.get();
            toggle.setEnabled(true);
        } else {
            toggle = new FeatureToggle(featureName, true);
            toggle.setDescription(description);
        }
        
        FeatureToggle saved = featureToggleRepository.save(toggle);
        logger.info("Feature '{}' has been enabled in database", featureName);
        return saved;
    }
    
    /**
     * Disable a feature (saves to database).
     * Note: YAML flags are read-only. Use this method for runtime changes.
     */
    public FeatureToggle disableFeature(String featureName) {
        // Warn if feature is defined in YAML (YAML takes precedence)
        if (properties.hasFlag(featureName)) {
            logger.warn("Feature '{}' is defined in YAML. YAML value will override DB value when reading.", featureName);
        }
        
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        if (existingToggle.isPresent()) {
            FeatureToggle toggle = existingToggle.get();
            toggle.setEnabled(false);
            FeatureToggle saved = featureToggleRepository.save(toggle);
            logger.info("Feature '{}' has been disabled in database", featureName);
            return saved;
        }
        
        logger.warn("Attempted to disable non-existent feature '{}'", featureName);
        throw new IllegalArgumentException("Feature toggle '" + featureName + "' does not exist in database");
    }
    
    /**
     * Toggle a feature (enable if disabled, disable if enabled).
     * Note: YAML flags are read-only. Use this method for runtime changes.
     */
    public FeatureToggle toggleFeature(String featureName) {
        // Warn if feature is defined in YAML (YAML takes precedence)
        if (properties.hasFlag(featureName)) {
            logger.warn("Feature '{}' is defined in YAML. YAML value will override DB value when reading.", featureName);
        }
        
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        if (existingToggle.isPresent()) {
            FeatureToggle toggle = existingToggle.get();
            toggle.setEnabled(!toggle.isEnabled());
            FeatureToggle saved = featureToggleRepository.save(toggle);
            logger.info("Feature '{}' toggled to {} in database", featureName, saved.isEnabled() ? "enabled" : "disabled");
            return saved;
        }
        
        logger.warn("Attempted to toggle non-existent feature '{}'", featureName);
        throw new IllegalArgumentException("Feature toggle '" + featureName + "' does not exist in database");
    }
    
    /**
     * Get all feature toggles from database.
     * Note: This only returns DB toggles. YAML flags are not included.
     * Use getAllFeatureFlags() to get combined view.
     */
    public List<FeatureToggle> getAllFeatures() {
        return featureToggleRepository.findAll();
    }
    
    /**
     * Get a specific feature toggle from database.
     * Note: This only checks DB. Use isFeatureEnabled() to check with YAML priority.
     */
    public Optional<FeatureToggle> getFeature(String featureName) {
        return featureToggleRepository.findByFeatureName(featureName);
    }
    
    /**
     * Get all feature flags (YAML + DB combined).
     * Returns a map with all feature flags, YAML flags take precedence.
     */
    public Map<String, Boolean> getAllFeatureFlags() {
        Map<String, Boolean> allFlags = new HashMap<>();
        
        // Add YAML flags first (they take precedence)
        allFlags.putAll(properties.getFlags());
        
        // Add DB flags (only if not already in YAML)
        if (properties.isDbFallbackEnabled()) {
            List<FeatureToggle> dbToggles = featureToggleRepository.findAll();
            for (FeatureToggle toggle : dbToggles) {
                allFlags.putIfAbsent(toggle.getFeatureName(), toggle.isEnabled());
            }
        }
        
        return allFlags;
    }
    
    /**
     * Create or update a feature toggle
     */
    public FeatureToggle saveFeatureToggle(String featureName, boolean enabled, String description) {
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        FeatureToggle toggle;
        if (existingToggle.isPresent()) {
            toggle = existingToggle.get();
            toggle.setEnabled(enabled);
            if (description != null) {
                toggle.setDescription(description);
            }
        } else {
            toggle = new FeatureToggle(featureName, enabled);
            toggle.setDescription(description);
        }
        
        return featureToggleRepository.save(toggle);
    }
    
    /**
     * Delete a feature toggle
     */
    public void deleteFeatureToggle(String featureName) {
        featureToggleRepository.deleteByFeatureName(featureName);
        logger.info("Feature toggle '{}' deleted", featureName);
    }
}
