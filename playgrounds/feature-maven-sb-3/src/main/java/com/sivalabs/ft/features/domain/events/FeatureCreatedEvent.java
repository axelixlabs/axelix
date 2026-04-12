package com.sivalabs.ft.features.domain.events;

import com.sivalabs.ft.features.domain.models.FeatureStatus;
import java.time.Instant;

public record FeatureCreatedEvent(
        Long id,
        String code,
        String title,
        String description,
        FeatureStatus status,
        String releaseCode,
        String assignedTo,
        String createdBy,
        Instant createdAt) {}
