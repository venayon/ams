package com.example.award.repository;

import com.example.award.config.TestContainersConfiguration;
import com.example.award.domain.Award;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AwardRepo repository
 * Uses Testcontainers to spin up a real MongoDB instance
 */
@DataMongoTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class AwardRepoRepositoryIntegrationTest {
    
    @Autowired
    @Qualifier("awardRepo")
    private AwardRepo awardRepoRepository;
    
    private Award testAward1;
    private Award testAward2;
    
    @BeforeEach
    void setUp() {
        awardRepoRepository.deleteAll();
        
        testAward1 = new Award("Award 1", "Description 1", "PERFORMANCE");
        testAward1.setStatus("PENDING");
        testAward1.setRecipientId("recipient-1");
        testAward1.setCreatedAt(LocalDateTime.now().minusDays(5));
        testAward1 = awardRepoRepository.save(testAward1);
        
        testAward2 = new Award("Award 2", "Description 2", "INNOVATION");
        testAward2.setStatus("APPROVED");
        testAward2.setRecipientId("recipient-2");
        testAward2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testAward2 = awardRepoRepository.save(testAward2);
    }
    
    @AfterEach
    void tearDown() {
        awardRepoRepository.deleteAll();
    }
    
    @Test
    void findByRecipientId_shouldReturnAwards() {
        // When
        List<Award> awards = awardRepoRepository.findByRecipientId("recipient-1");
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 1", awards.get(0).getName());
    }
    
    @Test
    void findByCategory_shouldReturnAwards() {
        // When
        List<Award> awards = awardRepoRepository.findByCategory("PERFORMANCE");
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 1", awards.get(0).getName());
    }
    
    @Test
    void findByStatus_shouldReturnAwards() {
        // When
        List<Award> pendingAwards = awardRepoRepository.findByStatus("PENDING");
        List<Award> approvedAwards = awardRepoRepository.findByStatus("APPROVED");
        
        // Then
        assertEquals(1, pendingAwards.size());
        assertEquals(1, approvedAwards.size());
        assertEquals("Award 1", pendingAwards.get(0).getName());
        assertEquals("Award 2", approvedAwards.get(0).getName());
    }
    
    @Test
    void findByCategoryAndStatus_shouldReturnMatchingAwards() {
        // When
        List<Award> awards = awardRepoRepository.findByCategoryAndStatus("PERFORMANCE", "PENDING");
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 1", awards.get(0).getName());
    }
    
    @Test
    void findByCreatedAtAfter_shouldReturnRecentAwards() {
        // When
        List<Award> awards = awardRepoRepository.findByCreatedAtAfter(
                LocalDateTime.now().minusDays(3)
        );
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 2", awards.get(0).getName());
    }
    
    @Test
    void findByName_shouldReturnAward() {
        // When
        Optional<Award> award = awardRepoRepository.findByName("Award 1");
        
        // Then
        assertTrue(award.isPresent());
        assertEquals("Award 1", award.get().getName());
    }
    
    @Test
    void countByStatus_shouldReturnCount() {
        // When
        long pendingCount = awardRepoRepository.countByStatus("PENDING");
        long approvedCount = awardRepoRepository.countByStatus("APPROVED");
        
        // Then
        assertEquals(1, pendingCount);
        assertEquals(1, approvedCount);
    }
    
    @Test
    void save_shouldPersistAward() {
        // Given
        Award newAward = new Award("New Award", "New Description", "TEST");
        
        // When
        Award saved = awardRepoRepository.save(newAward);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("New Award", saved.getName());
        
        // Verify persistence
        Optional<Award> found = awardRepoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
    
    @Test
    void delete_shouldRemoveAward() {
        // Given
        String awardId = testAward1.getId();
        
        // When
        awardRepoRepository.deleteById(awardId);
        
        // Then
        assertFalse(awardRepoRepository.existsById(awardId));
    }
    
    @Test
    void findAll_shouldReturnAllAwards() {
        // When
        List<Award> awards = awardRepoRepository.findAll();
        
        // Then
        assertEquals(2, awards.size());
    }
    
    @Test
    void count_shouldReturnTotalCount() {
        // When
        long count = awardRepoRepository.count();
        
        // Then
        assertEquals(2, count);
    }
}
