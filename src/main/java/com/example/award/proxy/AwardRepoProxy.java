package com.example.award.proxy;

import com.example.award.domain.Award;
import com.example.award.facade.AwardFacade;
import com.example.award.repository.OldFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * AwardRepoProxy - "Traffic cop"
 * 
 * This proxy implements the OldFlow interface and is marked as @Primary.
 * When service layer autowires OldFlow, Spring will inject this proxy instead.
 * The proxy delegates all calls to AwardFacade, which decides between OldFlow and NewFlow.
 * 
 * Flow: Service Layer → AwardRepoProxy (Traffic Cop) → AwardFacade (Decision Maker) → OldFlow/NewFlow
 */
@Repository
@Primary
@Qualifier("awardRepoProxy")
public class AwardRepoProxy implements OldFlow {
    
    private static final Logger logger = LoggerFactory.getLogger(AwardRepoProxy.class);
    
    private final AwardFacade awardFacade;
    
    @Autowired
    public AwardRepoProxy(AwardFacade awardFacade) {
        this.awardFacade = awardFacade;
    }
    
    // ========== MongoRepository Methods (delegated to facade) ==========
    
    // ========== MongoRepository Methods (delegated to facade) ==========
    
    @Override
    public <S extends Award> S save(S award) {
        logger.info("AwardRepoProxy: Saving award request received");
        
        // Pre-processing validation
        validateAward(award);
        
        // Delegate to facade
       
        S savedAward = (S) awardFacade.save(award);
        
        logger.info("AwardRepoProxy: Award saved successfully with ID: {}", savedAward.getId());
        return savedAward;
    }
    
    @Override
    public <S extends Award> List<S> saveAll(Iterable<S> entities) {
        logger.info("AwardRepoProxy: Saving multiple awards");
       
        List<S> saved = (List<S>) awardFacade.saveAll(entities);
        logger.info("AwardRepoProxy: {} awards saved", saved.size());
        return saved;
    }
    
    @Override
    public Optional<Award> findById(String id) {
        logger.debug("AwardRepoProxy: Finding award by ID: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            logger.warn("AwardRepoProxy: Invalid ID provided");
            throw new IllegalArgumentException("Award ID cannot be null or empty");
        }
        
        Optional<Award> award = awardFacade.findById(id);
        
        if (award.isPresent()) {
            logger.debug("AwardRepoProxy: Award found with ID: {}", id);
        } else {
            logger.debug("AwardRepoProxy: No award found with ID: {}", id);
        }
        
        return award;
    }
    
    @Override
    public boolean existsById(String id) {
        logger.debug("AwardRepoProxy: Checking existence of award with ID: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        boolean exists = awardFacade.existsById(id);
        logger.debug("AwardRepoProxy: Award with ID {} exists: {}", id, exists);
        return exists;
    }
    
    @Override
    public List<Award> findAll() {
        logger.debug("AwardRepoProxy: Finding all awards");
        List<Award> awards = awardFacade.findAll();
        logger.debug("AwardRepoProxy: Found {} awards", awards.size());
        return awards;
    }
    
    @Override
    public List<Award> findAll(Sort sort) {
        logger.debug("AwardRepoProxy: Finding all awards with sort");
        List<Award> awards = awardFacade.findAll(sort);
        logger.debug("AwardRepoProxy: Found {} awards", awards.size());
        return awards;
    }
    
    @Override
    public Page<Award> findAll(Pageable pageable) {
        logger.debug("AwardRepoProxy: Finding all awards with pagination");
        Page<Award> awards = awardFacade.findAll(pageable);
        logger.debug("AwardRepoProxy: Found {} awards", awards.getTotalElements());
        return awards;
    }
    
    @Override
    public List<Award> findAllById(Iterable<String> ids) {
        logger.debug("AwardRepoProxy: Finding awards by IDs");
        List<Award> awards = awardFacade.findAllById(ids);
        logger.debug("AwardRepoProxy: Found {} awards", awards.size());
        return awards;
    }
    
    @Override
    public long count() {
        logger.debug("AwardRepoProxy: Counting awards");
        long count = awardFacade.count();
        logger.debug("AwardRepoProxy: Total awards count: {}", count);
        return count;
    }
    
    @Override
    public void delete(Award award) {
        logger.info("AwardRepoProxy: Delete award request received");
        
        if (award == null) {
            logger.error("AwardRepoProxy: Cannot delete null award");
            throw new IllegalArgumentException("Award cannot be null");
        }
        
        awardFacade.delete(award);
        logger.info("AwardRepoProxy: Award deleted successfully");
    }
    
    @Override
    public void deleteById(String id) {
        logger.info("AwardRepoProxy: Delete award by ID request received: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            logger.error("AwardRepoProxy: Invalid ID for deletion");
            throw new IllegalArgumentException("Award ID cannot be null or empty");
        }
        
        awardFacade.deleteById(id);
        logger.info("AwardRepoProxy: Award deleted successfully with ID: {}", id);
    }
    
    @Override
    public void deleteAll() {
        logger.warn("AwardRepoProxy: Delete all awards request received");
        awardFacade.deleteAll();
        logger.info("AwardRepoProxy: All awards deleted");
    }
    
    @Override
    public void deleteAll(Iterable<? extends Award> entities) {
        logger.info("AwardRepoProxy: Delete multiple awards request received");
        awardFacade.deleteAll(entities);
        logger.info("AwardRepoProxy: Multiple awards deleted");
    }
    
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        logger.info("AwardRepoProxy: Delete awards by IDs request received");
        awardFacade.deleteAllById(ids);
        logger.info("AwardRepoProxy: Awards deleted by IDs");
    }
    
    // ========== OldFlow-specific query methods ==========
    
    @Override
    public List<Award> findByRecipientId(String recipientId) {
        logger.debug("AwardRepoProxy: Finding awards for recipient: {}", recipientId);
        
        if (recipientId == null || recipientId.trim().isEmpty()) {
            logger.warn("AwardRepoProxy: Invalid recipient ID");
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }
        
        List<Award> awards = awardFacade.findByRecipientId(recipientId);
        logger.debug("AwardRepoProxy: Found {} awards for recipient: {}", awards.size(), recipientId);
        return awards;
    }
    
    @Override
    public List<Award> findByCategory(String category) {
        logger.debug("AwardRepoProxy: Finding awards by category: {}", category);
        
        if (category == null || category.trim().isEmpty()) {
            logger.warn("AwardRepoProxy: Invalid category");
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        List<Award> awards = awardFacade.findByCategory(category);
        logger.debug("AwardRepoProxy: Found {} awards in category: {}", awards.size(), category);
        return awards;
    }
    
    @Override
    public List<Award> findByStatus(String status) {
        logger.debug("AwardRepoProxy: Finding awards by status: {}", status);
        
        if (status == null || status.trim().isEmpty()) {
            logger.warn("AwardRepoProxy: Invalid status");
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        List<Award> awards = awardFacade.findByStatus(status);
        logger.debug("AwardRepoProxy: Found {} awards with status: {}", awards.size(), status);
        return awards;
    }
    
    @Override
    public List<Award> findByCategoryAndStatus(String category, String status) {
        logger.debug("AwardRepoProxy: Finding awards by category: {} and status: {}", category, status);
        List<Award> awards = awardFacade.findByCategoryAndStatus(category, status);
        logger.debug("AwardRepoProxy: Found {} awards", awards.size());
        return awards;
    }
    
    @Override
    public List<Award> findByCreatedAtAfter(LocalDateTime date) {
        logger.debug("AwardRepoProxy: Finding awards created after: {}", date);
        List<Award> awards = awardFacade.findByCreatedAtAfter(date);
        logger.debug("AwardRepoProxy: Found {} awards", awards.size());
        return awards;
    }
    
    @Override
    public Optional<Award> findByName(String name) {
        logger.debug("AwardRepoProxy: Finding award by name: {}", name);
        Optional<Award> award = awardFacade.findByName(name);
        logger.debug("AwardRepoProxy: Award {} found: {}", name, award.isPresent());
        return award;
    }
    
    @Override
    public long countByStatus(String status) {
        logger.debug("AwardRepoProxy: Counting awards by status: {}", status);
        long count = awardFacade.countByStatus(status);
        logger.debug("AwardRepoProxy: Found {} awards with status: {}", count, status);
        return count;
    }
    
    // ========== Additional MongoRepository methods (delegated but not commonly used) ==========
    
    @Override
    public <S extends Award> Optional<S> findOne(Example<S> example) {
       
        Optional<S> result = (Optional<S>) awardFacade.findOne((Example<Award>) example);
        return result;
    }
    
    @Override
    public <S extends Award> List<S> findAll(Example<S> example) {
       
        List<S> result = (List<S>) awardFacade.findAll((Example<Award>) example);
        return result;
    }
    
    @Override
    public <S extends Award> List<S> findAll(Example<S> example, Sort sort) {
       
        List<S> result = (List<S>) awardFacade.findAll((Example<Award>) example, sort);
        return result;
    }
    
    @Override
    public <S extends Award> Page<S> findAll(Example<S> example, Pageable pageable) {
       
        Page<S> result = (Page<S>) awardFacade.findAll((Example<Award>) example, pageable);
        return result;
    }
    
    @Override
    public <S extends Award> long count(Example<S> example) {
       
        return awardFacade.count((Example<Award>) example);
    }
    
    @Override
    public <S extends Award> boolean exists(Example<S> example) {
       
        return awardFacade.exists((Example<Award>) example);
    }
    
    @Override
    public <S extends Award, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
       
        //return awardFacade.findBy((Example<Award>) example, (Function<FluentQuery.FetchableFluentQuery<Award>, R>) queryFunction);
        return null;
    }
    
    @Override
    public <S extends Award> S insert(S entity) {
       
        S result = (S) awardFacade.insert(entity);
        return result;
    }
    
    @Override
    public <S extends Award> List<S> insert(Iterable<S> entities) {
       
        List<S> result = (List<S>) awardFacade.insert((Iterable<Award>) entities);
        return result;
    }
    
    /**
     * Validate award before saving
     */
    private void validateAward(Award award) {
        if (award == null) {
            logger.error("AwardRepoProxy: Cannot save null award");
            throw new IllegalArgumentException("Award cannot be null");
        }
        
        if (award.getName() == null || award.getName().trim().isEmpty()) {
            logger.error("AwardRepoProxy: Award name is required");
            throw new IllegalArgumentException("Award name cannot be null or empty");
        }
        
        logger.debug("AwardRepoProxy: Award validation passed");
    }
}
