package com.sivalabs.ft.features.api.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateProductPayload(
        @Size(max = 10, message = "Product prefix cannot exceed 10 characters") @NotEmpty(message = "Product prefix is required") String prefix,
        @Size(max = 255, message = "Product name cannot exceed 255 characters") @NotEmpty(message = "Product name is required") String name,
        String description,
        String imageUrl) {}
