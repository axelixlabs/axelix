package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.models.FeatureStatus;
import com.sivalabs.ft.features.domain.models.ReleaseStatus;
import java.time.Instant;

public class Commands {
    private Commands() {}

    /* Product Commands */
    public record CreateProductCommand(
            String code, String prefix, String name, String description, String imageUrl, String createdBy) {}

    public record UpdateProductCommand(
            String code, String prefix, String name, String description, String imageUrl, String updatedBy) {}

    /* Release Commands */
    public record CreateReleaseCommand(String productCode, String code, String description, String createdBy) {}

    public record UpdateReleaseCommand(
            String code, String description, ReleaseStatus status, Instant releasedAt, String updatedBy) {}

    /* Feature Commands */
    public record CreateFeatureCommand(
            String productCode,
            String releaseCode,
            String title,
            String description,
            String assignedTo,
            String createdBy) {}

    public record UpdateFeatureCommand(
            String code,
            String title,
            String description,
            FeatureStatus status,
            String releaseCode,
            String assignedTo,
            String updatedBy) {}

    public record DeleteFeatureCommand(String code, String deletedBy) {}

    /* Comment Commands */
    public record CreateCommentCommand(String featureCode, String content, String createdBy) {}
}
