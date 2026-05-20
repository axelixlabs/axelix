package com.sivalabs.ft.features.api.models;

import jakarta.validation.constraints.NotBlank;

public record AddCommentPayload(@NotBlank String featureCode, @NotBlank String content) {}
