package com.example.award.facade;

import com.example.award.domain.Award;
import com.example.award.repository.AwardRepoV2;
import com.example.award.repository.AwardRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * AwardFacade - "Decision maker"
 * Decides which flow (Old or New) to use based on feature toggle
 * Acts as a facade to abstract the underlying repository implementation
 */
@Component
public class AwardFacade {
    
    private static final Logger logger = LoggerFactory.getLogger(AwardFacade.class);
    
    private final AwardRepo awardRepoRepository;
    private final AwardRepoV2 awardRepoV2Repository;
    private final FeatureToggleService featureToggleService;
    
    @Autowired
    public AwardFacade(
            @Qualifier("awardRepo") AwardRepo awardRepoRepository,
            @Qualifier("awardRepoV2") AwardRepoV2 awardRepoV2Repository,
            FeatureToggleService featureToggleService) {
        this.awardRepoRepository = awardRepoRepository;
        this.awardRepoV2Repository = awardRepoV2Repository;
        this.featureToggleService = featureToggleService;
    }
    
    /**
     * Determines which repository to use based on feature toggle
     */
    private MongoRepository<Award, String> determineFlow() {
        boolean useNewFlow = featureToggleService.isFeatureEnabled("use-new-flow");
        
        if (useNewFlow) {
            logger.debug("Using New Flow for award processing");
            return awardRepoV2Repository;
        } else {
            logger.debug("Using Old Flow for award processing");
            return awardRepoRepository;
        }
    }
    
    /**
     * Get the typed repository (AwardRepo or AwardRepoV2) for query-specific methods
     */
    private boolean isUsingNewFlow() {
        return featureToggleService.isFeatureEnabled("use-new-flow");
    }
    
    // ========== Standard MongoRepository Methods ==========
    
    public <S extends Award> S save(S award) {
        MongoRepository<Award, String> repository = determineFlow();
        logger.info("Saving award: {} using {}", award.getName(), 
                   repository == awardRepoV2Repository ? "AwardRepoV2" : "AwardRepo");
        return repository.save(award);
    }
    
    public <S extends Award> List<S> saveAll(Iterable<S> entities) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.saveAll(entities);
    }
    
    public Optional<Award> findById(String id) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findById(id);
    }
    
    public boolean existsById(String id) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.existsById(id);
    }
    
    public List<Award> findAll() {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll();
    }
    
    public List<Award> findAll(Sort sort) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll(sort);
    }
    
    public Page<Award> findAll(Pageable pageable) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll(pageable);
    }
    
    public List<Award> findAllById(Iterable<String> ids) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAllById(ids);
    }
    
    public long count() {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.count();
    }
    
    public void deleteById(String id) {
        MongoRepository<Award, String> repository = determineFlow();
        logger.info("Deleting award by id: {} using {}", id, 
                   repository == awardRepoV2Repository ? "AwardRepoV2" : "AwardRepo");
        repository.deleteById(id);
    }
    
    public void delete(Award award) {
        MongoRepository<Award, String> repository = determineFlow();
        logger.info("Deleting award: {} using {}", award.getId(), 
                   repository == awardRepoV2Repository ? "AwardRepoV2" : "AwardRepo");
        repository.delete(award);
    }
    
    public void deleteAllById(Iterable<? extends String> ids) {
        MongoRepository<Award, String> repository = determineFlow();
        repository.deleteAllById(ids);
    }
    
    public void deleteAll(Iterable<? extends Award> entities) {
        MongoRepository<Award, String> repository = determineFlow();
        repository.deleteAll(entities);
    }
    
    public void deleteAll() {
        MongoRepository<Award, String> repository = determineFlow();
        repository.deleteAll();
    }
    
    public <S extends Award> Optional<S> findOne(Example<S> example) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findOne(example);
    }
    
    public <S extends Award> List<S> findAll(Example<S> example) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll(example);
    }
    
    public <S extends Award> List<S> findAll(Example<S> example, Sort sort) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll(example, sort);
    }
    
    public <S extends Award> Page<S> findAll(Example<S> example, Pageable pageable) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findAll(example, pageable);
    }
    
    public <S extends Award> long count(Example<S> example) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.count(example);
    }
    
    public <S extends Award> boolean exists(Example<S> example) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.exists(example);
    }
    
    public <S extends Award, R> R findBy(Example<S> example, 
                                          Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.findBy(example, queryFunction);
    }
    
    public <S extends Award> S insert(S entity) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.insert(entity);
    }
    
    public <S extends Award> List<S> insert(Iterable<S> entities) {
        MongoRepository<Award, String> repository = determineFlow();
        return repository.insert(entities);
    }
    
    // ========== Query Methods (available in both AwardRepo and AwardRepoV2) ==========
    
    // ========== Query Methods (available in both AwardRepo and AwardRepoV2) ==========
    
    public List<Award> findByRecipientId(String recipientId) {
        if (isUsingNewFlow()) {
            return awardRepoV2Repository.findByRecipientId(recipientId);
        } else {
            return awardRepoRepository.findByRecipientId(recipientId);
        }
    }
    
    public List<Award> findByCategory(String category) {
        if (isUsingNewFlow()) {
            return awardRepoV2Repository.findByCategory(category);
        } else {
            return awardRepoRepository.findByCategory(category);
        }
    }
    
    public List<Award> findByStatus(String status) {
        if (isUsingNewFlow()) {
            return awardRepoV2Repository.findByStatus(status);
        } else {
            return awardRepoRepository.findByStatus(status);
        }
    }
    
    public List<Award> findByCategoryAndStatus(String category, String status) {
        if (isUsingNewFlow()) {
            return awardRepoV2Repository.findByCategoryAndStatus(category, status);
        } else {
            return awardRepoRepository.findByCategoryAndStatus(category, status);
        }
    }
    
    public Optional<Award> findByName(String name) {
        if (isUsingNewFlow()) {
            return awardRepoV2Repository.findByName(name);
        } else {
            return awardRepoRepository.findByName(name);
        }
    }
    
    // ========== AwardRepo-specific methods ==========
    
    public List<Award> findByCreatedAtAfter(LocalDateTime date) {
        if (isUsingNewFlow()) {
            // AwardRepoV2 doesn't have this method, use AwardRepo as fallback
            logger.warn("findByCreatedAtAfter not available in AwardRepoV2, using AwardRepo");
            return awardRepoRepository.findByCreatedAtAfter(date);
        } else {
            return awardRepoRepository.findByCreatedAtAfter(date);
        }
    }
    
    public long countByStatus(String status) {
        if (isUsingNewFlow()) {
            // AwardRepoV2 doesn't have this method, use AwardRepo as fallback
            logger.warn("countByStatus not available in AwardRepoV2, using AwardRepo");
            return awardRepoRepository.countByStatus(status);
        } else {
            return awardRepoRepository.countByStatus(status);
        }
    }
}
