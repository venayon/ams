package com.example.award.repository;

import com.example.award.domain.FeatureToggle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Feature Toggle management
 * Extends MongoRepository for standard CRUD operations
 */
@Repository
public interface FeatureToggleRepository extends MongoRepository<FeatureToggle, String> {
    
    /**
     * Find feature toggle by feature name
     */
    Optional<FeatureToggle> findByFeatureName(String featureName);
    
    /**
     * Find all enabled features
     */
    List<FeatureToggle> findByEnabled(boolean enabled);
    
    /**
     * Check if a feature exists by name
     */
    boolean existsByFeatureName(String featureName);
    
    /**
     * Delete feature toggle by feature name
     */
    void deleteByFeatureName(String featureName);
}
