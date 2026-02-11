package com.example.award.controller;

import com.example.award.config.TestContainersConfiguration;
import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FeatureToggleController
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class FeatureToggleControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private FeatureToggleRepository featureToggleRepository;
    
    @BeforeEach
    void setUp() {
        featureToggleRepository.deleteAll();
    }
    
    @Test
    void getAllFeatureToggles_shouldReturnEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature-toggles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void getAllFeatureToggles_shouldReturnAllToggles() throws Exception {
        // Given
        featureToggleRepository.save(new FeatureToggle("feature1", true));
        featureToggleRepository.save(new FeatureToggle("feature2", false));
        
        // When & Then
        mockMvc.perform(get("/api/feature-toggles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
    
    @Test
    void getFeatureToggle_shouldReturnToggle() throws Exception {
        // Given
        FeatureToggle toggle = new FeatureToggle("test-feature", true);
        toggle.setDescription("Test description");
        featureToggleRepository.save(toggle);
        
        // When & Then
        mockMvc.perform(get("/api/feature-toggles/test-feature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureName").value("test-feature"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.description").value("Test description"));
    }
    
    @Test
    void getFeatureToggle_shouldReturn404WhenNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature-toggles/non-existent"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void isFeatureEnabled_shouldReturnEnabledStatus() throws Exception {
        // Given
        featureToggleRepository.save(new FeatureToggle("enabled-feature", true));
        
        // When & Then
        mockMvc.perform(get("/api/feature-toggles/enabled-feature/enabled"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    @Test
    void isFeatureEnabled_shouldReturnFalseWhenNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/feature-toggles/non-existent/enabled"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
    
    @Test
    void saveFeatureToggle_shouldCreateNewToggle() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("featureName", "new-feature");
        request.put("enabled", true);
        request.put("description", "New feature description");
        
        // When & Then
        mockMvc.perform(post("/api/feature-toggles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.featureName").value("new-feature"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.description").value("New feature description"));
    }
    
    @Test
    void enableFeature_shouldEnableExistingToggle() throws Exception {
        // Given
        FeatureToggle toggle = new FeatureToggle("test-feature", false);
        featureToggleRepository.save(toggle);
        
        // When & Then
        mockMvc.perform(post("/api/feature-toggles/test-feature/enable")
                        .param("description", "Updated description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
    
    @Test
    void enableFeature_shouldCreateNewToggleIfNotExists() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/feature-toggles/new-feature/enable")
                        .param("description", "New feature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureName").value("new-feature"))
                .andExpect(jsonPath("$.enabled").value(true));
    }
    
    @Test
    void disableFeature_shouldDisableToggle() throws Exception {
        // Given
        featureToggleRepository.save(new FeatureToggle("test-feature", true));
        
        // When & Then
        mockMvc.perform(post("/api/feature-toggles/test-feature/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }
    
    @Test
    void disableFeature_shouldReturn404WhenNotFound() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/feature-toggles/non-existent/disable"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void toggleFeature_shouldFlipEnabledState() throws Exception {
        // Given
        featureToggleRepository.save(new FeatureToggle("test-feature", true));
        
        // When & Then - First toggle (true -> false)
        mockMvc.perform(post("/api/feature-toggles/test-feature/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
        
        // When & Then - Second toggle (false -> true)
        mockMvc.perform(post("/api/feature-toggles/test-feature/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
    
    @Test
    void toggleFeature_shouldReturn404WhenNotFound() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/feature-toggles/non-existent/toggle"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void deleteFeatureToggle_shouldDeleteToggle() throws Exception {
        // Given
        featureToggleRepository.save(new FeatureToggle("test-feature", true));
        
        // When & Then
        mockMvc.perform(delete("/api/feature-toggles/test-feature"))
                .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/feature-toggles/test-feature"))
                .andExpect(status().isNotFound());
    }
}
