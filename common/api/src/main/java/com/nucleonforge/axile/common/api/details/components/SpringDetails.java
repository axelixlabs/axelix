package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record SpringDetails(
        @JsonProperty("springBootVersion") String springBootVersion,
        @JsonProperty("springVersion") String springVersion,
        @JsonProperty("springCloudVersion") @Nullable String springCloudVersion) {}
