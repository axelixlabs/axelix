package com.sivalabs.ft.features.domain.dtos;

import com.sivalabs.ft.features.domain.models.FeatureStatus;
import java.io.Serializable;
import java.time.Instant;

public record FeatureDto(
        Long id,
        String code,
        String title,
        String description,
        FeatureStatus status,
        String releaseCode,
        boolean isFavorite,
        String assignedTo,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt)
        implements Serializable {

    public FeatureDto makeFavorite(boolean favorite) {
        return new FeatureDto(
                id,
                code,
                title,
                description,
                status,
                releaseCode,
                favorite,
                assignedTo,
                createdBy,
                createdAt,
                updatedBy,
                updatedAt);
    }
}
