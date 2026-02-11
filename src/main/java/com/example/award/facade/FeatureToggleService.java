package com.example.award.facade;

import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing feature toggles
 * Provides methods to check, enable, and disable features
 */
@Service
public class FeatureToggleService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureToggleService.class);
    
    private final FeatureToggleRepository featureToggleRepository;
    
    @Autowired
    public FeatureToggleService(FeatureToggleRepository featureToggleRepository) {
        this.featureToggleRepository = featureToggleRepository;
    }
    
    /**
     * Check if a feature is enabled
     * Returns false if feature toggle doesn't exist
     */
    public boolean isFeatureEnabled(String featureName) {
        Optional<FeatureToggle> toggle = featureToggleRepository.findByFeatureName(featureName);
        
        if (toggle.isPresent()) {
            boolean enabled = toggle.get().isEnabled();
            logger.debug("Feature '{}' is {}", featureName, enabled ? "enabled" : "disabled");
            return enabled;
        }
        
        logger.warn("Feature toggle '{}' not found, defaulting to disabled", featureName);
        return false;
    }
    
    /**
     * Enable a feature
     * Creates the feature toggle if it doesn't exist
     */
    public FeatureToggle enableFeature(String featureName, String description) {
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
        logger.info("Feature '{}' has been enabled", featureName);
        return saved;
    }
    
    /**
     * Disable a feature
     */
    public FeatureToggle disableFeature(String featureName) {
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        if (existingToggle.isPresent()) {
            FeatureToggle toggle = existingToggle.get();
            toggle.setEnabled(false);
            FeatureToggle saved = featureToggleRepository.save(toggle);
            logger.info("Feature '{}' has been disabled", featureName);
            return saved;
        }
        
        logger.warn("Attempted to disable non-existent feature '{}'", featureName);
        throw new IllegalArgumentException("Feature toggle '" + featureName + "' does not exist");
    }
    
    /**
     * Toggle a feature (enable if disabled, disable if enabled)
     */
    public FeatureToggle toggleFeature(String featureName) {
        Optional<FeatureToggle> existingToggle = featureToggleRepository.findByFeatureName(featureName);
        
        if (existingToggle.isPresent()) {
            FeatureToggle toggle = existingToggle.get();
            toggle.setEnabled(!toggle.isEnabled());
            FeatureToggle saved = featureToggleRepository.save(toggle);
            logger.info("Feature '{}' toggled to {}", featureName, saved.isEnabled() ? "enabled" : "disabled");
            return saved;
        }
        
        logger.warn("Attempted to toggle non-existent feature '{}'", featureName);
        throw new IllegalArgumentException("Feature toggle '" + featureName + "' does not exist");
    }
    
    /**
     * Get all feature toggles
     */
    public List<FeatureToggle> getAllFeatures() {
        return featureToggleRepository.findAll();
    }
    
    /**
     * Get a specific feature toggle
     */
    public Optional<FeatureToggle> getFeature(String featureName) {
        return featureToggleRepository.findByFeatureName(featureName);
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
