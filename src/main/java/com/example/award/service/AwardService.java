package com.example.award.service;

import com.example.award.domain.Award;
import com.example.award.repository.OldFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer - Your Code
 * Business logic for award management
 * 
 * NOTE: This service injects OldFlow, but due to @Primary annotation on AwardRepoProxy,
 * Spring will actually inject the AwardRepoProxy instance.
 * 
 * Flow: AwardService → OldFlow (actually AwardRepoProxy) → AwardFacade → OldFlow/NewFlow
 */
@Service
@Transactional
public class AwardService {
    
    private static final Logger logger = LoggerFactory.getLogger(AwardService.class);
    
    // Injecting OldFlow, but AwardRepoProxy (marked @Primary) will be injected instead
    private final OldFlow awardRepository;
    
    @Autowired
    public AwardService(OldFlow awardRepository) {
        this.awardRepository = awardRepository;
    }
    
    /**
     * Create a new award
     */
    public Award createAward(Award award) {
        logger.info("Creating new award: {}", award.getName());
        
        // Business logic: Set creation timestamp
        award.setCreatedAt(LocalDateTime.now());
        award.setUpdatedAt(LocalDateTime.now());
        award.setStatus("PENDING");
        
        Award savedAward = awardRepository.save(award);
        logger.info("Award created successfully with ID: {}", savedAward.getId());
        
        return savedAward;
    }
    
    /**
     * Update an existing award
     */
    public Award updateAward(String id, Award updatedAward) {
        logger.info("Updating award with ID: {}", id);
        
        Optional<Award> existingAward = awardRepository.findById(id);
        
        if (existingAward.isEmpty()) {
            logger.error("Award not found with ID: {}", id);
            throw new IllegalArgumentException("Award not found with ID: " + id);
        }
        
        Award award = existingAward.get();
        
        // Update fields
        if (updatedAward.getName() != null) {
            award.setName(updatedAward.getName());
        }
        if (updatedAward.getDescription() != null) {
            award.setDescription(updatedAward.getDescription());
        }
        if (updatedAward.getCategory() != null) {
            award.setCategory(updatedAward.getCategory());
        }
        if (updatedAward.getRecipientId() != null) {
            award.setRecipientId(updatedAward.getRecipientId());
        }
        if (updatedAward.getStatus() != null) {
            award.setStatus(updatedAward.getStatus());
        }
        
        award.setUpdatedAt(LocalDateTime.now());
        
        Award saved = awardRepository.save(award);
        logger.info("Award updated successfully: {}", id);
        
        return saved;
    }
    
    /**
     * Get award by ID
     */
    public Optional<Award> getAwardById(String id) {
        logger.debug("Retrieving award with ID: {}", id);
        return awardRepository.findById(id);
    }
    
    /**
     * Get all awards
     */
    public List<Award> getAllAwards() {
        logger.debug("Retrieving all awards");
        return awardRepository.findAll();
    }
    
    /**
     * Delete award by ID
     */
    public void deleteAward(String id) {
        logger.info("Deleting award with ID: {}", id);
        
        if (!awardRepository.existsById(id)) {
            logger.error("Cannot delete - Award not found with ID: {}", id);
            throw new IllegalArgumentException("Award not found with ID: " + id);
        }
        
        awardRepository.deleteById(id);
        logger.info("Award deleted successfully: {}", id);
    }
    
    /**
     * Award the prize to a recipient
     */
    public Award awardToRecipient(String awardId, String recipientId) {
        logger.info("Awarding prize {} to recipient {}", awardId, recipientId);
        
        Optional<Award> existingAward = awardRepository.findById(awardId);
        
        if (existingAward.isEmpty()) {
            logger.error("Award not found with ID: {}", awardId);
            throw new IllegalArgumentException("Award not found with ID: " + awardId);
        }
        
        Award award = existingAward.get();
        
        // Business logic: Award to recipient
        award.setRecipientId(recipientId);
        award.setAwardedDate(LocalDateTime.now());
        award.setStatus("AWARDED");
        award.setUpdatedAt(LocalDateTime.now());
        
        Award saved = awardRepository.save(award);
        logger.info("Award {} successfully awarded to recipient {}", awardId, recipientId);
        
        return saved;
    }
    
    /**
     * Get awards by recipient
     */
    public List<Award> getAwardsByRecipient(String recipientId) {
        logger.debug("Retrieving awards for recipient: {}", recipientId);
        return awardRepository.findByRecipientId(recipientId);
    }
    
    /**
     * Get awards by category
     */
    public List<Award> getAwardsByCategory(String category) {
        logger.debug("Retrieving awards by category: {}", category);
        return awardRepository.findByCategory(category);
    }
    
    /**
     * Get awards by status
     */
    public List<Award> getAwardsByStatus(String status) {
        logger.debug("Retrieving awards by status: {}", status);
        return awardRepository.findByStatus(status);
    }
    
    /**
     * Get total count of awards
     */
    public long getTotalAwardsCount() {
        logger.debug("Getting total awards count");
        return awardRepository.count();
    }
    
    /**
     * Approve an award (change status to APPROVED)
     */
    public Award approveAward(String awardId) {
        logger.info("Approving award: {}", awardId);
        
        Optional<Award> existingAward = awardRepository.findById(awardId);
        
        if (existingAward.isEmpty()) {
            logger.error("Award not found with ID: {}", awardId);
            throw new IllegalArgumentException("Award not found with ID: " + awardId);
        }
        
        Award award = existingAward.get();
        award.setStatus("APPROVED");
        award.setUpdatedAt(LocalDateTime.now());
        
        Award saved = awardRepository.save(award);
        logger.info("Award approved: {}", awardId);
        
        return saved;
    }
    
    /**
     * Reject an award (change status to REJECTED)
     */
    public Award rejectAward(String awardId) {
        logger.info("Rejecting award: {}", awardId);
        
        Optional<Award> existingAward = awardRepository.findById(awardId);
        
        if (existingAward.isEmpty()) {
            logger.error("Award not found with ID: {}", awardId);
            throw new IllegalArgumentException("Award not found with ID: " + awardId);
        }
        
        Award award = existingAward.get();
        award.setStatus("REJECTED");
        award.setUpdatedAt(LocalDateTime.now());
        
        Award saved = awardRepository.save(award);
        logger.info("Award rejected: {}", awardId);
        
        return saved;
    }
}
