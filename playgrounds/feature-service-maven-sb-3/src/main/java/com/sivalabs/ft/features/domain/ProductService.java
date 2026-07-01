package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.Commands.CreateProductCommand;
import com.sivalabs.ft.features.domain.Commands.UpdateProductCommand;
import com.sivalabs.ft.features.domain.dtos.ProductDto;
import com.sivalabs.ft.features.domain.entities.Product;
import com.sivalabs.ft.features.domain.exceptions.ResourceNotFoundException;
import com.sivalabs.ft.features.domain.mappers.ProductMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> findProductByCode(String code) {
        return productRepository.findByCode(code).map(productMapper::toDto);
    }

    @Transactional
    public Long createProduct(CreateProductCommand cmd) {
        var product = new Product();
        product.setCode(cmd.code());
        product.setPrefix(cmd.prefix());
        product.setName(cmd.name());
        product.setDescription(cmd.description());
        product.setImageUrl(cmd.imageUrl());
        product.setCreatedBy(cmd.createdBy());
        product.setDisabled(false);
        Product savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }

    @Transactional
    public void updateProduct(UpdateProductCommand cmd) {
        var product = productRepository
                .findByCode(cmd.code())
                .orElseThrow(() -> new ResourceNotFoundException("Product %s not found".formatted(cmd)));
        product.setPrefix(cmd.prefix());
        product.setName(cmd.name());
        product.setDescription(cmd.description());
        product.setImageUrl(cmd.imageUrl());
        product.setUpdatedBy(cmd.updatedBy());
        productRepository.save(product);
    }
}
