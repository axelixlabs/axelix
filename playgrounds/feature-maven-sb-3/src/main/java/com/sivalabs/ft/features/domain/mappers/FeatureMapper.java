package com.sivalabs.ft.features.domain.mappers;

import com.sivalabs.ft.features.domain.dtos.FeatureDto;
import com.sivalabs.ft.features.domain.entities.Feature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureMapper {
    @Mapping(target = "releaseCode", source = "release.code", defaultExpression = "java( null )")
    @Mapping(target = "isFavorite", ignore = true)
    FeatureDto toDto(Feature feature);
}
