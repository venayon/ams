package com.example.award.service;

import com.example.award.domain.Award;
import com.example.award.repository.OldFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AwardService
 * Uses Mockito to mock repository dependencies
 */
@ExtendWith(MockitoExtension.class)
class AwardServiceTest {
    
    @Mock
    private OldFlow awardRepository;
    
    @InjectMocks
    private AwardService awardService;
    
    private Award testAward;
    
    @BeforeEach
    void setUp() {
        testAward = new Award();
        testAward.setId("test-id-123");
        testAward.setName("Test Award");
        testAward.setDescription("Test Description");
        testAward.setCategory("PERFORMANCE");
        testAward.setStatus("PENDING");
        testAward.setCreatedAt(LocalDateTime.now());
        testAward.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void createAward_shouldSetDefaultValues() {
        // Given
        Award newAward = new Award("New Award", "Description", "PERFORMANCE");
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> {
            Award award = invocation.getArgument(0);
            award.setId("generated-id");
            return award;
        });
        
        // When
        Award created = awardService.createAward(newAward);
        
        // Then
        assertNotNull(created);
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertEquals("PENDING", created.getStatus());
        verify(awardRepository, times(1)).save(any(Award.class));
    }
    
    @Test
    void updateAward_shouldUpdateOnlyProvidedFields() {
        // Given
        String awardId = "test-id-123";
        Award updateData = new Award();
        updateData.setName("Updated Name");
        updateData.setStatus("APPROVED");
        
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(testAward));
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Award updated = awardService.updateAward(awardId, updateData);
        
        // Then
        assertEquals("Updated Name", updated.getName());
        assertEquals("APPROVED", updated.getStatus());
        assertEquals("Test Description", updated.getDescription()); // Unchanged
        verify(awardRepository, times(1)).findById(awardId);
        verify(awardRepository, times(1)).save(any(Award.class));
    }
    
    @Test
    void updateAward_shouldThrowExceptionWhenNotFound() {
        // Given
        when(awardRepository.findById(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            awardService.updateAward("non-existent", new Award());
        });
        verify(awardRepository, never()).save(any(Award.class));
    }
    
    @Test
    void getAwardById_shouldReturnAward() {
        // Given
        when(awardRepository.findById("test-id-123")).thenReturn(Optional.of(testAward));
        
        // When
        Optional<Award> found = awardService.getAwardById("test-id-123");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Award", found.get().getName());
        verify(awardRepository, times(1)).findById("test-id-123");
    }
    
    @Test
    void getAllAwards_shouldReturnAllAwards() {
        // Given
        List<Award> awards = Arrays.asList(testAward, new Award("Award 2", "Desc", "CAT"));
        when(awardRepository.findAll()).thenReturn(awards);
        
        // When
        List<Award> result = awardService.getAllAwards();
        
        // Then
        assertEquals(2, result.size());
        verify(awardRepository, times(1)).findAll();
    }
    
    @Test
    void deleteAward_shouldDeleteExistingAward() {
        // Given
        when(awardRepository.existsById("test-id-123")).thenReturn(true);
        doNothing().when(awardRepository).deleteById("test-id-123");
        
        // When
        awardService.deleteAward("test-id-123");
        
        // Then
        verify(awardRepository, times(1)).existsById("test-id-123");
        verify(awardRepository, times(1)).deleteById("test-id-123");
    }
    
    @Test
    void deleteAward_shouldThrowExceptionWhenNotFound() {
        // Given
        when(awardRepository.existsById(anyString())).thenReturn(false);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            awardService.deleteAward("non-existent");
        });
        verify(awardRepository, never()).deleteById(anyString());
    }
    
    @Test
    void awardToRecipient_shouldSetRecipientAndStatus() {
        // Given
        String recipientId = "recipient-123";
        when(awardRepository.findById("test-id-123")).thenReturn(Optional.of(testAward));
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Award awarded = awardService.awardToRecipient("test-id-123", recipientId);
        
        // Then
        assertEquals(recipientId, awarded.getRecipientId());
        assertEquals("AWARDED", awarded.getStatus());
        assertNotNull(awarded.getAwardedDate());
        verify(awardRepository, times(1)).save(any(Award.class));
    }
    
    @Test
    void approveAward_shouldChangeStatusToApproved() {
        // Given
        when(awardRepository.findById("test-id-123")).thenReturn(Optional.of(testAward));
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Award approved = awardService.approveAward("test-id-123");
        
        // Then
        assertEquals("APPROVED", approved.getStatus());
        verify(awardRepository, times(1)).save(any(Award.class));
    }
    
    @Test
    void rejectAward_shouldChangeStatusToRejected() {
        // Given
        when(awardRepository.findById("test-id-123")).thenReturn(Optional.of(testAward));
        when(awardRepository.save(any(Award.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Award rejected = awardService.rejectAward("test-id-123");
        
        // Then
        assertEquals("REJECTED", rejected.getStatus());
        verify(awardRepository, times(1)).save(any(Award.class));
    }
    
    @Test
    void getAwardsByRecipient_shouldReturnRecipientAwards() {
        // Given
        String recipientId = "recipient-123";
        List<Award> awards = Arrays.asList(testAward);
        when(awardRepository.findByRecipientId(recipientId)).thenReturn(awards);
        
        // When
        List<Award> result = awardService.getAwardsByRecipient(recipientId);
        
        // Then
        assertEquals(1, result.size());
        verify(awardRepository, times(1)).findByRecipientId(recipientId);
    }
    
    @Test
    void getAwardsByCategory_shouldReturnCategoryAwards() {
        // Given
        String category = "PERFORMANCE";
        List<Award> awards = Arrays.asList(testAward);
        when(awardRepository.findByCategory(category)).thenReturn(awards);
        
        // When
        List<Award> result = awardService.getAwardsByCategory(category);
        
        // Then
        assertEquals(1, result.size());
        verify(awardRepository, times(1)).findByCategory(category);
    }
    
    @Test
    void getAwardsByStatus_shouldReturnStatusAwards() {
        // Given
        String status = "PENDING";
        List<Award> awards = Arrays.asList(testAward);
        when(awardRepository.findByStatus(status)).thenReturn(awards);
        
        // When
        List<Award> result = awardService.getAwardsByStatus(status);
        
        // Then
        assertEquals(1, result.size());
        verify(awardRepository, times(1)).findByStatus(status);
    }
    
    @Test
    void getTotalAwardsCount_shouldReturnCount() {
        // Given
        when(awardRepository.count()).thenReturn(5L);
        
        // When
        long count = awardService.getTotalAwardsCount();
        
        // Then
        assertEquals(5L, count);
        verify(awardRepository, times(1)).count();
    }
}
