package com.example.award.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for feature toggles.
 * Reads from application.yml with clean, simple structure.
 * 
 * Example YAML:
 * feature:
 *   toggle:
 *     db-fallback-enabled: true
 *     flags:
 *       use-new-flow: false
 *       new-feature: true
 */
@Component
@ConfigurationProperties(prefix = "feature.toggle")
public class FeatureToggleProperties {
    
    // Note: This class is not final to allow mocking in tests
    
    /**
     * If true, checks database when feature not found in YAML
     */
    private boolean dbFallbackEnabled = true;
    
    /**
     * Feature flags defined in application.yml
     */
    private Map<String, Boolean> flags = new HashMap<>();
    
    public boolean isDbFallbackEnabled() {
        return dbFallbackEnabled;
    }
    
    public void setDbFallbackEnabled(boolean dbFallbackEnabled) {
        this.dbFallbackEnabled = dbFallbackEnabled;
    }
    
    public Map<String, Boolean> getFlags() {
        return flags;
    }
    
    public void setFlags(Map<String, Boolean> flags) {
        this.flags = flags;
    }
    
    /**
     * Get feature flag value from YAML
     * @param featureName Feature name
     * @return Feature value or null if not found
     */
    public Boolean getFlag(String featureName) {
        return flags.get(featureName);
    }
    
    /**
     * Check if feature exists in YAML
     */
    public boolean hasFlag(String featureName) {
        return flags.containsKey(featureName);
    }
}
