package com.sivalabs.ft.features.domain.mappers;

import com.sivalabs.ft.features.domain.dtos.ProductDto;
import com.sivalabs.ft.features.domain.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
}
