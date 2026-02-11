package com.example.award.facade;

import com.example.award.domain.Award;
import com.example.award.repository.NewFlow;
import com.example.award.repository.OldFlow;
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
    
    private final OldFlow oldFlowRepository;
    private final NewFlow newFlowRepository;
    private final FeatureToggleService featureToggleService;
    
    @Autowired
    public AwardFacade(
            @Qualifier("oldFlow") OldFlow oldFlowRepository,
            @Qualifier("newFlow") NewFlow newFlowRepository,
            FeatureToggleService featureToggleService) {
        this.oldFlowRepository = oldFlowRepository;
        this.newFlowRepository = newFlowRepository;
        this.featureToggleService = featureToggleService;
    }
    
    /**
     * Determines which repository to use based on feature toggle
     */
    private MongoRepository<Award, String> determineFlow() {
        boolean useNewFlow = featureToggleService.isFeatureEnabled("use-new-flow");
        
        if (useNewFlow) {
            logger.debug("Using New Flow for award processing");
            return newFlowRepository;
        } else {
            logger.debug("Using Old Flow for award processing");
            return oldFlowRepository;
        }
    }
    
    /**
     * Get the typed repository (OldFlow or NewFlow) for query-specific methods
     */
    private boolean isUsingNewFlow() {
        return featureToggleService.isFeatureEnabled("use-new-flow");
    }
    
    // ========== Standard MongoRepository Methods ==========
    
    public <S extends Award> S save(S award) {
        MongoRepository<Award, String> repository = determineFlow();
        logger.info("Saving award: {} using {}", award.getName(), 
                   repository == newFlowRepository ? "NewFlow" : "OldFlow");
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
                   repository == newFlowRepository ? "NewFlow" : "OldFlow");
        repository.deleteById(id);
    }
    
    public void delete(Award award) {
        MongoRepository<Award, String> repository = determineFlow();
        logger.info("Deleting award: {} using {}", award.getId(), 
                   repository == newFlowRepository ? "NewFlow" : "OldFlow");
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
    
    // ========== Query Methods (available in both OldFlow and NewFlow) ==========
    
    // ========== Query Methods (available in both OldFlow and NewFlow) ==========
    
    public List<Award> findByRecipientId(String recipientId) {
        if (isUsingNewFlow()) {
            return newFlowRepository.findByRecipientId(recipientId);
        } else {
            return oldFlowRepository.findByRecipientId(recipientId);
        }
    }
    
    public List<Award> findByCategory(String category) {
        if (isUsingNewFlow()) {
            return newFlowRepository.findByCategory(category);
        } else {
            return oldFlowRepository.findByCategory(category);
        }
    }
    
    public List<Award> findByStatus(String status) {
        if (isUsingNewFlow()) {
            return newFlowRepository.findByStatus(status);
        } else {
            return oldFlowRepository.findByStatus(status);
        }
    }
    
    public List<Award> findByCategoryAndStatus(String category, String status) {
        if (isUsingNewFlow()) {
            return newFlowRepository.findByCategoryAndStatus(category, status);
        } else {
            return oldFlowRepository.findByCategoryAndStatus(category, status);
        }
    }
    
    public Optional<Award> findByName(String name) {
        if (isUsingNewFlow()) {
            return newFlowRepository.findByName(name);
        } else {
            return oldFlowRepository.findByName(name);
        }
    }
    
    // ========== OldFlow-specific methods ==========
    
    public List<Award> findByCreatedAtAfter(LocalDateTime date) {
        if (isUsingNewFlow()) {
            // NewFlow doesn't have this method, use OldFlow as fallback
            logger.warn("findByCreatedAtAfter not available in NewFlow, using OldFlow");
            return oldFlowRepository.findByCreatedAtAfter(date);
        } else {
            return oldFlowRepository.findByCreatedAtAfter(date);
        }
    }
    
    public long countByStatus(String status) {
        if (isUsingNewFlow()) {
            // NewFlow doesn't have this method, use OldFlow as fallback
            logger.warn("countByStatus not available in NewFlow, using OldFlow");
            return oldFlowRepository.countByStatus(status);
        } else {
            return oldFlowRepository.countByStatus(status);
        }
    }
}
