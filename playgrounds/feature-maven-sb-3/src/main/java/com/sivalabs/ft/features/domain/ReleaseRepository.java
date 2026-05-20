package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.Release;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

interface ReleaseRepository extends ListCrudRepository<Release, Long> {

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("releases")
    Optional<Release> findByCode(String code);

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("releases")
    List<Release> findByProductCode(String productCode);

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "releases", allEntries = true)
    @Modifying
    void deleteByCode(String code);

    @Transactional(readOnly = true)
    boolean existsByCode(String code);
}
