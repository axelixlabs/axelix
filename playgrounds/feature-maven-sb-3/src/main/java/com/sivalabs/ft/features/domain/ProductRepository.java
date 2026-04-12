package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.Product;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

interface ProductRepository extends ListCrudRepository<Product, Long> {

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("products")
    Optional<Product> findByCode(String code);
}
