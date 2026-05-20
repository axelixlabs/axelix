package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.Feature;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

interface FeatureRepository extends ListCrudRepository<Feature, Long> {

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("features")
    @Query("select f from Feature f left join fetch f.release where f.code = :code")
    Optional<Feature> findByCode(String code);

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("features")
    @Query("select f from Feature f left join fetch f.release where f.release.code = :releaseCode")
    List<Feature> findByReleaseCode(String releaseCode);

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("features")
    @Query("select f from Feature f left join fetch f.release where f.product.code = :productCode")
    List<Feature> findByProductCode(String productCode);

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "features", allEntries = true)
    @Modifying
    void deleteByCode(String code);

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "features", allEntries = true)
    @Modifying
    @Query("delete from Feature f where f.release.code = :code")
    void deleteByReleaseCode(String code);

    @Transactional(readOnly = true)
    boolean existsByCode(String code);

    @Transactional
    @Query(value = "select nextval('feature_code_seq')", nativeQuery = true)
    long getNextFeatureId();
}
