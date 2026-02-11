package com.example.award.repository;

import com.example.award.domain.Award;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for New Flow awards processing
 * Extends MongoRepository for standard CRUD operations
 * Includes enhanced query capabilities for the new processing flow
 */
@Repository
@Qualifier("newFlow")
public interface NewFlow extends MongoRepository<Award, String> {
    
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
     * Find awards awarded between two dates
     */
    List<Award> findByAwardedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find award by name
     */
    Optional<Award> findByName(String name);
    
    /**
     * Custom query to find awards with specific criteria
     */
    @Query("{ 'status': ?0, 'category': ?1, 'awardedDate': { $gte: ?2 } }")
    List<Award> findAwardsByStatusCategoryAndDate(String status, String category, LocalDateTime date);
    
    /**
     * Count awards by category
     */
    long countByCategory(String category);
    
    /**
     * Find top N awards by awarded date
     */
    List<Award> findTop10ByOrderByAwardedDateDesc();
}
