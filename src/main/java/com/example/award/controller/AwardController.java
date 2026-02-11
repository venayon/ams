package com.example.award.controller;

import com.example.award.domain.Award;
import com.example.award.service.AwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Award management
 */
@RestController
@RequestMapping("/api/awards")
public class AwardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AwardController.class);
    
    private final AwardService awardService;
    
    @Autowired
    public AwardController(AwardService awardService) {
        this.awardService = awardService;
    }
    
    /**
     * Create a new award
     * POST /api/awards
     */
    @PostMapping
    public ResponseEntity<Award> createAward(@RequestBody Award award) {
        logger.info("REST: Create award request received");
        Award createdAward = awardService.createAward(award);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAward);
    }
    
    /**
     * Get all awards
     * GET /api/awards
     */
    @GetMapping
    public ResponseEntity<List<Award>> getAllAwards() {
        logger.info("REST: Get all awards request received");
        List<Award> awards = awardService.getAllAwards();
        return ResponseEntity.ok(awards);
    }
    
    /**
     * Get award by ID
     * GET /api/awards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Award> getAwardById(@PathVariable String id) {
        logger.info("REST: Get award by ID request received: {}", id);
        Optional<Award> award = awardService.getAwardById(id);
        
        return award.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update an award
     * PUT /api/awards/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Award> updateAward(@PathVariable String id, @RequestBody Award award) {
        logger.info("REST: Update award request received for ID: {}", id);
        
        try {
            Award updatedAward = awardService.updateAward(id, award);
            return ResponseEntity.ok(updatedAward);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete an award
     * DELETE /api/awards/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAward(@PathVariable String id) {
        logger.info("REST: Delete award request received for ID: {}", id);
        
        try {
            awardService.deleteAward(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Award to a recipient
     * POST /api/awards/{id}/award
     */
    @PostMapping("/{id}/award")
    public ResponseEntity<Award> awardToRecipient(@PathVariable String id, @RequestParam String recipientId) {
        logger.info("REST: Award to recipient request received. Award: {}, Recipient: {}", id, recipientId);
        
        try {
            Award awardedPrize = awardService.awardToRecipient(id, recipientId);
            return ResponseEntity.ok(awardedPrize);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get awards by recipient
     * GET /api/awards/recipient/{recipientId}
     */
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<Award>> getAwardsByRecipient(@PathVariable String recipientId) {
        logger.info("REST: Get awards by recipient request received: {}", recipientId);
        List<Award> awards = awardService.getAwardsByRecipient(recipientId);
        return ResponseEntity.ok(awards);
    }
    
    /**
     * Get awards by category
     * GET /api/awards/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Award>> getAwardsByCategory(@PathVariable String category) {
        logger.info("REST: Get awards by category request received: {}", category);
        List<Award> awards = awardService.getAwardsByCategory(category);
        return ResponseEntity.ok(awards);
    }
    
    /**
     * Get awards by status
     * GET /api/awards/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Award>> getAwardsByStatus(@PathVariable String status) {
        logger.info("REST: Get awards by status request received: {}", status);
        List<Award> awards = awardService.getAwardsByStatus(status);
        return ResponseEntity.ok(awards);
    }
    
    /**
     * Get total count of awards
     * GET /api/awards/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getAwardsCount() {
        logger.info("REST: Get awards count request received");
        long count = awardService.getTotalAwardsCount();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Approve an award
     * POST /api/awards/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Award> approveAward(@PathVariable String id) {
        logger.info("REST: Approve award request received for ID: {}", id);
        
        try {
            Award approvedAward = awardService.approveAward(id);
            return ResponseEntity.ok(approvedAward);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Reject an award
     * POST /api/awards/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Award> rejectAward(@PathVariable String id) {
        logger.info("REST: Reject award request received for ID: {}", id);
        
        try {
            Award rejectedAward = awardService.rejectAward(id);
            return ResponseEntity.ok(rejectedAward);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
