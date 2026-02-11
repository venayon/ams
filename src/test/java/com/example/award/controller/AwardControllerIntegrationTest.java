package com.example.award.controller;

import com.example.award.config.TestContainersConfiguration;
import com.example.award.domain.Award;
import com.example.award.domain.FeatureToggle;
import com.example.award.repository.FeatureToggleRepository;
import com.example.award.repository.OldFlow;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AwardController
 * Uses Testcontainers for MongoDB and MockMvc for REST API testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class AwardControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OldFlow awardRepository;
    
    @Autowired
    private FeatureToggleRepository featureToggleRepository;
    
    @BeforeEach
    void setUp() {
        awardRepository.deleteAll();
        featureToggleRepository.deleteAll();
        
        // Initialize feature toggle as disabled (use OldFlow)
        FeatureToggle toggle = new FeatureToggle("use-new-flow", false);
        toggle.setDescription("Test toggle");
        featureToggleRepository.save(toggle);
    }
    
    @Test
    void createAward_shouldReturnCreated() throws Exception {
        // Given
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        
        // When & Then
        mockMvc.perform(post("/api/awards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(award)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Award"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void getAllAwards_shouldReturnAllAwards() throws Exception {
        // Given
        Award award1 = new Award("Award 1", "Desc 1", "CAT1");
        Award award2 = new Award("Award 2", "Desc 2", "CAT2");
        awardRepository.save(award1);
        awardRepository.save(award2);
        
        // When & Then
        mockMvc.perform(get("/api/awards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Award 1"))
                .andExpect(jsonPath("$[1].name").value("Award 2"));
    }
    
    @Test
    void getAwardById_shouldReturnAward() throws Exception {
        // Given
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        Award saved = awardRepository.save(award);
        
        // When & Then
        mockMvc.perform(get("/api/awards/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Test Award"));
    }
    
    @Test
    void getAwardById_shouldReturn404WhenNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/awards/non-existent-id"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void updateAward_shouldReturnUpdatedAward() throws Exception {
        // Given
        Award award = new Award("Original Name", "Description", "PERFORMANCE");
        Award saved = awardRepository.save(award);
        
        Award updateData = new Award();
        updateData.setName("Updated Name");
        updateData.setStatus("APPROVED");
        
        // When & Then
        mockMvc.perform(put("/api/awards/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
    
    @Test
    void deleteAward_shouldReturn204() throws Exception {
        // Given
        Award award = new Award("To Delete", "Description", "PERFORMANCE");
        Award saved = awardRepository.save(award);
        
        // When & Then
        mockMvc.perform(delete("/api/awards/" + saved.getId()))
                .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/awards/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void awardToRecipient_shouldSetRecipient() throws Exception {
        // Given
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        Award saved = awardRepository.save(award);
        
        // When & Then
        mockMvc.perform(post("/api/awards/" + saved.getId() + "/award")
                        .param("recipientId", "recipient-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientId").value("recipient-123"))
                .andExpect(jsonPath("$.status").value("AWARDED"))
                .andExpect(jsonPath("$.awardedDate").exists());
    }
    
    @Test
    void getAwardsByRecipient_shouldReturnFilteredAwards() throws Exception {
        // Given
        Award award1 = new Award("Award 1", "Desc", "CAT");
        award1.setRecipientId("recipient-123");
        Award award2 = new Award("Award 2", "Desc", "CAT");
        award2.setRecipientId("recipient-456");
        awardRepository.save(award1);
        awardRepository.save(award2);
        
        // When & Then
        mockMvc.perform(get("/api/awards/recipient/recipient-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientId").value("recipient-123"));
    }
    
    @Test
    void getAwardsByCategory_shouldReturnFilteredAwards() throws Exception {
        // Given
        Award award1 = new Award("Award 1", "Desc", "PERFORMANCE");
        Award award2 = new Award("Award 2", "Desc", "INNOVATION");
        awardRepository.save(award1);
        awardRepository.save(award2);
        
        // When & Then
        mockMvc.perform(get("/api/awards/category/PERFORMANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("PERFORMANCE"));
    }
    
    @Test
    void getAwardsByStatus_shouldReturnFilteredAwards() throws Exception {
        // Given
        Award award1 = new Award("Award 1", "Desc", "CAT");
        award1.setStatus("PENDING");
        Award award2 = new Award("Award 2", "Desc", "CAT");
        award2.setStatus("APPROVED");
        awardRepository.save(award1);
        awardRepository.save(award2);
        
        // When & Then
        mockMvc.perform(get("/api/awards/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
    
    @Test
    void approveAward_shouldChangeStatus() throws Exception {
        // Given
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        award.setStatus("PENDING");
        Award saved = awardRepository.save(award);
        
        // When & Then
        mockMvc.perform(post("/api/awards/" + saved.getId() + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
    
    @Test
    void rejectAward_shouldChangeStatus() throws Exception {
        // Given
        Award award = new Award("Test Award", "Description", "PERFORMANCE");
        award.setStatus("PENDING");
        Award saved = awardRepository.save(award);
        
        // When & Then
        mockMvc.perform(post("/api/awards/" + saved.getId() + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
    
    @Test
    void getAwardsCount_shouldReturnTotalCount() throws Exception {
        // Given
        awardRepository.save(new Award("Award 1", "Desc", "CAT"));
        awardRepository.save(new Award("Award 2", "Desc", "CAT"));
        awardRepository.save(new Award("Award 3", "Desc", "CAT"));
        
        // When & Then
        mockMvc.perform(get("/api/awards/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
