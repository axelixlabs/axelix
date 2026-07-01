package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.Release;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.ListCrudRepository;

interface ReleaseRepository extends ListCrudRepository<Release, Long> {
    Optional<Release> findByCode(String code);

    List<Release> findByProductCode(String productCode);

    @Modifying
    void deleteByCode(String code);

    boolean existsByCode(String code);
}
