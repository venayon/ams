package com.example.award.controller;

import com.example.award.domain.FeatureToggle;
import com.example.award.facade.FeatureToggleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Feature Toggle management
 */
@RestController
@RequestMapping("/api/feature-toggles")
public class FeatureToggleController {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureToggleController.class);
    
    private final FeatureToggleService featureToggleService;
    
    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }
    
    /**
     * Get all feature toggles
     * GET /api/feature-toggles
     */
    @GetMapping
    public ResponseEntity<List<FeatureToggle>> getAllFeatureToggles() {
        logger.info("REST: Get all feature toggles request received");
        List<FeatureToggle> toggles = featureToggleService.getAllFeatures();
        return ResponseEntity.ok(toggles);
    }
    
    /**
     * Get feature toggle by name
     * GET /api/feature-toggles/{featureName}
     */
    @GetMapping("/{featureName}")
    public ResponseEntity<FeatureToggle> getFeatureToggle(@PathVariable String featureName) {
        logger.info("REST: Get feature toggle request received: {}", featureName);
        Optional<FeatureToggle> toggle = featureToggleService.getFeature(featureName);
        
        return toggle.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Check if feature is enabled
     * GET /api/feature-toggles/{featureName}/enabled
     */
    @GetMapping("/{featureName}/enabled")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String featureName) {
        logger.info("REST: Check feature enabled request received: {}", featureName);
        boolean enabled = featureToggleService.isFeatureEnabled(featureName);
        return ResponseEntity.ok(enabled);
    }
    
    /**
     * Create or update a feature toggle
     * POST /api/feature-toggles
     */
    @PostMapping
    public ResponseEntity<FeatureToggle> saveFeatureToggle(@RequestBody FeatureToggleRequest request) {
        logger.info("REST: Save feature toggle request received: {}", request.getFeatureName());
        
        FeatureToggle toggle = featureToggleService.saveFeatureToggle(
            request.getFeatureName(),
            request.isEnabled(),
            request.getDescription()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toggle);
    }
    
    /**
     * Enable a feature
     * POST /api/feature-toggles/{featureName}/enable
     */
    @PostMapping("/{featureName}/enable")
    public ResponseEntity<FeatureToggle> enableFeature(@PathVariable String featureName, 
                                                       @RequestParam(required = false) String description) {
        logger.info("REST: Enable feature request received: {}", featureName);
        FeatureToggle toggle = featureToggleService.enableFeature(featureName, description);
        return ResponseEntity.ok(toggle);
    }
    
    /**
     * Disable a feature
     * POST /api/feature-toggles/{featureName}/disable
     */
    @PostMapping("/{featureName}/disable")
    public ResponseEntity<FeatureToggle> disableFeature(@PathVariable String featureName) {
        logger.info("REST: Disable feature request received: {}", featureName);
        
        try {
            FeatureToggle toggle = featureToggleService.disableFeature(featureName);
            return ResponseEntity.ok(toggle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Toggle a feature (enable if disabled, disable if enabled)
     * POST /api/feature-toggles/{featureName}/toggle
     */
    @PostMapping("/{featureName}/toggle")
    public ResponseEntity<FeatureToggle> toggleFeature(@PathVariable String featureName) {
        logger.info("REST: Toggle feature request received: {}", featureName);
        
        try {
            FeatureToggle toggle = featureToggleService.toggleFeature(featureName);
            return ResponseEntity.ok(toggle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a feature toggle
     * DELETE /api/feature-toggles/{featureName}
     */
    @DeleteMapping("/{featureName}")
    public ResponseEntity<Void> deleteFeatureToggle(@PathVariable String featureName) {
        logger.info("REST: Delete feature toggle request received: {}", featureName);
        featureToggleService.deleteFeatureToggle(featureName);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Request DTO for creating/updating feature toggles
     */
    public static class FeatureToggleRequest {
        private String featureName;
        private boolean enabled;
        private String description;
        
        public FeatureToggleRequest() {}
        
        public FeatureToggleRequest(String featureName, boolean enabled, String description) {
            this.featureName = featureName;
            this.enabled = enabled;
            this.description = description;
        }
        
        public String getFeatureName() {
            return featureName;
        }
        
        public void setFeatureName(String featureName) {
            this.featureName = featureName;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
