package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomInfo(
        @JsonProperty("kotlinVersion") String kotlinVersion,
        @JsonProperty("springFrameworkVersion") String springFrameworkVersion,
        @JsonProperty("springCloudVersion") String springCloudVersion) {}
