package com.sivalabs.ft.features.domain.dtos;

import com.sivalabs.ft.features.domain.models.ReleaseStatus;
import java.io.Serializable;
import java.time.Instant;

public record ReleaseDto(
        Long id,
        String code,
        String description,
        ReleaseStatus status,
        Instant releasedAt,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt)
        implements Serializable {}
