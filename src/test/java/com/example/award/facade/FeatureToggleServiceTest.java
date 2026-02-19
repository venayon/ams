package com.example.award.facade;

import com.example.award.config.FeatureToggleProperties;
import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeatureToggleService
 */
@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {
    
    @Mock
    private FeatureToggleRepository featureToggleRepository;
    
    private FeatureToggleProperties properties;
    
    private FeatureToggleService featureToggleService;
    
    private FeatureToggle testToggle;
    
    @BeforeEach
    void setUp() {
        testToggle = new FeatureToggle("test-feature", true);
        testToggle.setId("toggle-id-123");
        testToggle.setDescription("Test feature toggle");
        
        // Create real instance of properties (not mocked)
        properties = new FeatureToggleProperties();
        properties.setDbFallbackEnabled(true);
        properties.setFlags(new HashMap<>());
        
        // Create service with real properties and mocked repository
        featureToggleService = new FeatureToggleService(featureToggleRepository, properties);
    }
    
    @Test
    void isFeatureEnabled_shouldReturnTrueWhenEnabled() {
        // Given - not in YAML, check DB
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        
        // When
        boolean enabled = featureToggleService.isFeatureEnabled("test-feature");
        
        // Then
        assertTrue(enabled);
        verify(featureToggleRepository, times(1)).findByFeatureName("test-feature");
    }
    
    @Test
    void isFeatureEnabled_shouldReturnFalseWhenDisabled() {
        // Given - not in YAML, check DB
        testToggle.setEnabled(false);
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        
        // When
        boolean enabled = featureToggleService.isFeatureEnabled("test-feature");
        
        // Then
        assertFalse(enabled);
    }
    
    @Test
    void isFeatureEnabled_shouldReturnFalseWhenNotFound() {
        // Given - not in YAML, not in DB
        when(featureToggleRepository.findByFeatureName(anyString()))
                .thenReturn(Optional.empty());
        
        // When
        boolean enabled = featureToggleService.isFeatureEnabled("non-existent");
        
        // Then
        assertFalse(enabled);
    }
    
    @Test
    void isFeatureEnabled_shouldReturnYamlValueWhenInYaml() {
        // Given - feature exists in YAML
        Map<String, Boolean> yamlFlags = new HashMap<>();
        yamlFlags.put("yaml-feature", true);
        properties.setFlags(yamlFlags);
        
        // When
        boolean enabled = featureToggleService.isFeatureEnabled("yaml-feature");
        
        // Then
        assertTrue(enabled);
        verify(featureToggleRepository, never()).findByFeatureName(anyString());
    }

    /**
     * When DB fallback is disabled, repository must not be called for unknown flags.
     */
    @Test
    void isFeatureEnabled_shouldNotCheckDbWhenFallbackDisabled() {
        properties.setDbFallbackEnabled(false);
        // No stub: we assert the repository is never called

        boolean enabled = featureToggleService.isFeatureEnabled("any-feature");

        assertFalse(enabled, "When not in YAML and fallback disabled, should default to false");
        verify(featureToggleRepository, never()).findByFeatureName(anyString());
    }
    
    @Test
    void enableFeature_shouldCreateNewToggleIfNotExists() {
        // Given
        when(featureToggleRepository.findByFeatureName("new-feature"))
                .thenReturn(Optional.empty());
        when(featureToggleRepository.save(any(FeatureToggle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FeatureToggle result = featureToggleService.enableFeature("new-feature", "Description");
        
        // Then
        assertTrue(result.isEnabled());
        assertEquals("new-feature", result.getFeatureName());
        verify(featureToggleRepository, times(1)).save(any(FeatureToggle.class));
    }
    
    @Test
    void enableFeature_shouldUpdateExistingToggle() {
        // Given
        testToggle.setEnabled(false);
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        when(featureToggleRepository.save(any(FeatureToggle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FeatureToggle result = featureToggleService.enableFeature("test-feature", "Updated");
        
        // Then
        assertTrue(result.isEnabled());
        verify(featureToggleRepository, times(1)).save(any(FeatureToggle.class));
    }
    
    @Test
    void disableFeature_shouldDisableExistingToggle() {
        // Given
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        when(featureToggleRepository.save(any(FeatureToggle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FeatureToggle result = featureToggleService.disableFeature("test-feature");
        
        // Then
        assertFalse(result.isEnabled());
        verify(featureToggleRepository, times(1)).save(any(FeatureToggle.class));
    }
    
    @Test
    void disableFeature_shouldThrowExceptionWhenNotFound() {
        // Given
        when(featureToggleRepository.findByFeatureName(anyString()))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            featureToggleService.disableFeature("non-existent");
        });
        verify(featureToggleRepository, never()).save(any(FeatureToggle.class));
    }
    
    @Test
    void toggleFeature_shouldFlipEnabledState() {
        // Given
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        when(featureToggleRepository.save(any(FeatureToggle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FeatureToggle result = featureToggleService.toggleFeature("test-feature");
        
        // Then
        assertFalse(result.isEnabled()); // Was true, now false
        verify(featureToggleRepository, times(1)).save(any(FeatureToggle.class));
    }
    
    @Test
    void getAllFeatures_shouldReturnAllToggles() {
        // Given
        List<FeatureToggle> toggles = Arrays.asList(
                testToggle,
                new FeatureToggle("feature2", false)
        );
        when(featureToggleRepository.findAll()).thenReturn(toggles);
        
        // When
        List<FeatureToggle> result = featureToggleService.getAllFeatures();
        
        // Then
        assertEquals(2, result.size());
        verify(featureToggleRepository, times(1)).findAll();
    }
    
    @Test
    void getFeature_shouldReturnToggle() {
        // Given
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.of(testToggle));
        
        // When
        Optional<FeatureToggle> result = featureToggleService.getFeature("test-feature");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("test-feature", result.get().getFeatureName());
    }
    
    @Test
    void saveFeatureToggle_shouldCreateOrUpdate() {
        // Given
        when(featureToggleRepository.findByFeatureName("test-feature"))
                .thenReturn(Optional.empty());
        when(featureToggleRepository.save(any(FeatureToggle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FeatureToggle result = featureToggleService.saveFeatureToggle(
                "test-feature", true, "Description"
        );
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEnabled());
        verify(featureToggleRepository, times(1)).save(any(FeatureToggle.class));
    }
    
    @Test
    void deleteFeatureToggle_shouldDeleteToggle() {
        // Given
        doNothing().when(featureToggleRepository).deleteByFeatureName("test-feature");
        
        // When
        featureToggleService.deleteFeatureToggle("test-feature");
        
        // Then
        verify(featureToggleRepository, times(1)).deleteByFeatureName("test-feature");
    }
}
