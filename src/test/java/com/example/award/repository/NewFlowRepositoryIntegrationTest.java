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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for NewFlow repository
 * Uses Testcontainers for MongoDB
 */
@DataMongoTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class NewFlowRepositoryIntegrationTest {
    
    @Autowired
    @Qualifier("newFlow")
    private NewFlow newFlowRepository;
    
    private Award testAward1;
    private Award testAward2;
    
    @BeforeEach
    void setUp() {
        newFlowRepository.deleteAll();
        
        testAward1 = new Award("Award 1", "Description 1", "PERFORMANCE");
        testAward1.setStatus("PENDING");
        testAward1.setAwardedDate(LocalDateTime.now().minusDays(5));
        testAward1 = newFlowRepository.save(testAward1);
        
        testAward2 = new Award("Award 2", "Description 2", "INNOVATION");
        testAward2.setStatus("APPROVED");
        testAward2.setAwardedDate(LocalDateTime.now().minusDays(2));
        testAward2 = newFlowRepository.save(testAward2);
    }
    
    @AfterEach
    void tearDown() {
        newFlowRepository.deleteAll();
    }
    
    @Test
    void findByAwardedDateBetween_shouldReturnAwardsInRange() {
        // When
        List<Award> awards = newFlowRepository.findByAwardedDateBetween(
                LocalDateTime.now().minusDays(6),
                LocalDateTime.now().minusDays(1)
        );
        
        // Then
        assertEquals(2, awards.size());
    }
    
    @Test
    void findAwardsByStatusCategoryAndDate_shouldReturnMatchingAwards() {
        // When
        List<Award> awards = newFlowRepository.findAwardsByStatusCategoryAndDate(
                "PENDING",
                "PERFORMANCE",
                LocalDateTime.now().minusDays(10)
        );
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 1", awards.get(0).getName());
    }
    
    @Test
    void countByCategory_shouldReturnCount() {
        // When
        long performanceCount = newFlowRepository.countByCategory("PERFORMANCE");
        long innovationCount = newFlowRepository.countByCategory("INNOVATION");
        
        // Then
        assertEquals(1, performanceCount);
        assertEquals(1, innovationCount);
    }
    
    @Test
    void findTop10ByOrderByAwardedDateDesc_shouldReturnMostRecent() {
        // Given - add more awards
        for (int i = 3; i <= 12; i++) {
            Award award = new Award("Award " + i, "Desc", "CAT");
            award.setAwardedDate(LocalDateTime.now().minusDays(i));
            newFlowRepository.save(award);
        }
        
        // When
        List<Award> topAwards = newFlowRepository.findTop10ByOrderByAwardedDateDesc();
        
        // Then
        assertTrue(topAwards.size() <= 10);
        // First award should be most recent
        assertTrue(topAwards.get(0).getAwardedDate()
                .isAfter(topAwards.get(topAwards.size() - 1).getAwardedDate()));
    }
    
    @Test
    void findByRecipientId_shouldReturnAwards() {
        // Given
        testAward1.setRecipientId("recipient-123");
        newFlowRepository.save(testAward1);
        
        // When
        List<Award> awards = newFlowRepository.findByRecipientId("recipient-123");
        
        // Then
        assertEquals(1, awards.size());
    }
    
    @Test
    void findByCategory_shouldReturnAwards() {
        // When
        List<Award> awards = newFlowRepository.findByCategory("PERFORMANCE");
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 1", awards.get(0).getName());
    }
    
    @Test
    void findByStatus_shouldReturnAwards() {
        // When
        List<Award> awards = newFlowRepository.findByStatus("APPROVED");
        
        // Then
        assertEquals(1, awards.size());
        assertEquals("Award 2", awards.get(0).getName());
    }
}
