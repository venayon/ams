package com.example.award.repository;

import com.example.award.domain.Award;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Old Flow awards processing
 * Extends MongoRepository for standard CRUD operations
 */
@Repository
@Qualifier("oldFlow")
public interface OldFlow extends MongoRepository<Award, String> {
    
    /**
     * Find awards by recipient ID
     */
    List<Award> findByRecipientId(String recipientId);
    
    /**
     * Find awards by category
     */
    List<Award> findByCategory(String category);
    
    /**
     * Find awards by status
     */
    List<Award> findByStatus(String status);
    
    /**
     * Find awards by category and status
     */
    List<Award> findByCategoryAndStatus(String category, String status);
    
    /**
     * Find awards created after a specific date
     */
    List<Award> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find award by name
     */
    Optional<Award> findByName(String name);
    
    /**
     * Count awards by status
     */
    long countByStatus(String status);
}
