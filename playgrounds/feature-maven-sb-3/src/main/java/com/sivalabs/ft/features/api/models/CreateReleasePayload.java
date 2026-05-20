package com.sivalabs.ft.features.api.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateReleasePayload(
        @NotEmpty(message = "Product code is required") String productCode,
        @Size(max = 50, message = "Release code cannot exceed 50 characters") @NotEmpty(message = "Release code is required") String code,
        String description) {}
