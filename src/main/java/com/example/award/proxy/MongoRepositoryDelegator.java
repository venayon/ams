package com.example.award.proxy;

import com.example.award.domain.Award;
import com.example.award.facade.AwardFacade;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper class that delegates all MongoRepository methods to AwardFacade.
 * This allows AwardRepoProxy to focus only on custom AwardRepo methods
 * while maintaining full MongoRepository functionality.
 */
class MongoRepositoryDelegator {
    
    private final AwardFacade awardFacade;
    
    MongoRepositoryDelegator(AwardFacade awardFacade) {
        this.awardFacade = awardFacade;
    }
    
    // ========== MongoRepository Methods ==========
    
    <S extends Award> S save(S entity) {
        return (S) awardFacade.save(entity);
    }
    
    <S extends Award> List<S> saveAll(Iterable<S> entities) {
        return (List<S>) awardFacade.saveAll(entities);
    }
    
    Optional<Award> findById(String id) {
        return awardFacade.findById(id);
    }
    
    boolean existsById(String id) {
        return awardFacade.existsById(id);
    }
    
    List<Award> findAll() {
        return awardFacade.findAll();
    }
    
    List<Award> findAll(Sort sort) {
        return awardFacade.findAll(sort);
    }
    
    Page<Award> findAll(Pageable pageable) {
        return awardFacade.findAll(pageable);
    }
    
    List<Award> findAllById(Iterable<String> ids) {
        return awardFacade.findAllById(ids);
    }
    
    long count() {
        return awardFacade.count();
    }
    
    void deleteById(String id) {
        awardFacade.deleteById(id);
    }
    
    void delete(Award entity) {
        awardFacade.delete(entity);
    }
    
    void deleteAllById(Iterable<? extends String> ids) {
        awardFacade.deleteAllById(ids);
    }
    
    void deleteAll(Iterable<? extends Award> entities) {
        awardFacade.deleteAll(entities);
    }
    
    void deleteAll() {
        awardFacade.deleteAll();
    }
    
    <S extends Award> Optional<S> findOne(Example<S> example) {
        return (Optional<S>) awardFacade.findOne((Example<Award>) example);
    }
    
    <S extends Award> List<S> findAll(Example<S> example) {
        return (List<S>) awardFacade.findAll((Example<Award>) example);
    }
    
    <S extends Award> List<S> findAll(Example<S> example, Sort sort) {
        return (List<S>) awardFacade.findAll((Example<Award>) example, sort);
    }
    
    <S extends Award> Page<S> findAll(Example<S> example, Pageable pageable) {
        return (Page<S>) awardFacade.findAll((Example<Award>) example, pageable);
    }
    
    <S extends Award> long count(Example<S> example) {
        return awardFacade.count((Example<Award>) example);
    }
    
    <S extends Award> boolean exists(Example<S> example) {
        return awardFacade.exists((Example<Award>) example);
    }
    
    <S extends Award, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return awardFacade.findBy(example, queryFunction);
    }
    
    <S extends Award> S insert(S entity) {
        return (S) awardFacade.insert(entity);
    }
    
    <S extends Award> List<S> insert(Iterable<S> entities) {
        return (List<S>) awardFacade.insert((Iterable<Award>) entities);
    }
}
