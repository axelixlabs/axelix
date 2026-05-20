package com.sivalabs.ft.features.domain.mappers;

import com.sivalabs.ft.features.domain.dtos.ReleaseDto;
import com.sivalabs.ft.features.domain.entities.Release;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReleaseMapper {
    ReleaseDto toDto(Release release);
}
