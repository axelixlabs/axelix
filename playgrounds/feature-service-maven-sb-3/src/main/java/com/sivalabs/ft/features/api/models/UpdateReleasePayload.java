package com.sivalabs.ft.features.api.models;

import com.sivalabs.ft.features.domain.models.ReleaseStatus;
import java.time.Instant;

public record UpdateReleasePayload(String description, ReleaseStatus status, Instant releasedAt) {}
