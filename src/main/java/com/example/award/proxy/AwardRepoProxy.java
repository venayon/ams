package com.example.award.proxy;

import com.example.award.domain.Award;
import com.example.award.facade.AwardFacade;
import com.example.award.repository.AwardRepo;
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
 * This proxy implements the AwardRepo interface and is marked as @Primary.
 * When service layer autowires AwardRepo, Spring will inject this proxy instead.
 * The proxy delegates all calls to AwardFacade, which decides between AwardRepo and AwardRepoV2.
 * 
 * Flow: Service Layer → AwardRepoProxy (Traffic Cop) → AwardFacade (Decision Maker) → AwardRepo/AwardRepoV2
 * 
 * This proxy focuses on implementing only the custom methods from AwardRepo.
 * All MongoRepository methods are delegated to AwardFacade with minimal implementation.
 */
@Repository
@Primary
@Qualifier("awardRepoProxy")
public class AwardRepoProxy implements AwardRepo {
    
    private static final Logger logger = LoggerFactory.getLogger(AwardRepoProxy.class);
    
    private final AwardFacade awardFacade;
    private final MongoRepositoryDelegator mongoDelegator;
    
    @Autowired
    public AwardRepoProxy(AwardFacade awardFacade) {
        this.awardFacade = awardFacade;
        this.mongoDelegator = new MongoRepositoryDelegator(awardFacade);
    }
    
    // ========== AwardRepo Custom Methods (explicitly implemented) ==========
    
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
    
    // ========== MongoRepository Methods (delegated via MongoRepositoryDelegator) ==========
    // All standard MongoRepository methods are delegated through MongoRepositoryDelegator.
    // This keeps the proxy file clean and focused on custom AwardRepo methods above.
    // Full MongoRepository functionality is maintained through delegation.
    
    @Override
    public <S extends Award> S save(S entity) {
        logger.info("AwardRepoProxy: Saving award request received");
        validateAward(entity);
        S savedAward = mongoDelegator.save(entity);
        logger.info("AwardRepoProxy: Award saved successfully with ID: {}", savedAward.getId());
        return savedAward;
    }
    
    @Override
    public <S extends Award> List<S> saveAll(Iterable<S> entities) {
        return mongoDelegator.saveAll(entities);
    }
    
    @Override
    public Optional<Award> findById(String id) {
        return mongoDelegator.findById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return mongoDelegator.existsById(id);
    }
    
    @Override
    public List<Award> findAll() {
        return mongoDelegator.findAll();
    }
    
    @Override
    public List<Award> findAll(Sort sort) {
        return mongoDelegator.findAll(sort);
    }
    
    @Override
    public Page<Award> findAll(Pageable pageable) {
        return mongoDelegator.findAll(pageable);
    }
    
    @Override
    public List<Award> findAllById(Iterable<String> ids) {
        return mongoDelegator.findAllById(ids);
    }
    
    @Override
    public long count() {
        return mongoDelegator.count();
    }
    
    @Override
    public void deleteById(String id) {
        mongoDelegator.deleteById(id);
    }
    
    @Override
    public void delete(Award entity) {
        mongoDelegator.delete(entity);
    }
    
    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        mongoDelegator.deleteAllById(ids);
    }
    
    @Override
    public void deleteAll(Iterable<? extends Award> entities) {
        mongoDelegator.deleteAll(entities);
    }
    
    @Override
    public void deleteAll() {
        mongoDelegator.deleteAll();
    }
    
    @Override
    public <S extends Award> Optional<S> findOne(Example<S> example) {
        return mongoDelegator.findOne(example);
    }
    
    @Override
    public <S extends Award> List<S> findAll(Example<S> example) {
        return mongoDelegator.findAll(example);
    }
    
    @Override
    public <S extends Award> List<S> findAll(Example<S> example, Sort sort) {
        return mongoDelegator.findAll(example, sort);
    }
    
    @Override
    public <S extends Award> Page<S> findAll(Example<S> example, Pageable pageable) {
        return mongoDelegator.findAll(example, pageable);
    }
    
    @Override
    public <S extends Award> long count(Example<S> example) {
        return mongoDelegator.count(example);
    }
    
    @Override
    public <S extends Award> boolean exists(Example<S> example) {
        return mongoDelegator.exists(example);
    }
    
    @Override
    public <S extends Award, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return mongoDelegator.findBy(example, queryFunction);
    }
    
    @Override
    public <S extends Award> S insert(S entity) {
        return mongoDelegator.insert(entity);
    }
    
    @Override
    public <S extends Award> List<S> insert(Iterable<S> entities) {
        return mongoDelegator.insert(entities);
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
