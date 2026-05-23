package com.sivalabs.ft.features.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ft.openapi")
@Validated
public record OpenAPIProperties(
        @DefaultValue("FeatureService API") String title,
        @DefaultValue("FeatureService API Swagger Documentation") String description,
        @DefaultValue("v1.0.0") String version,
        Contact contact) {

    public record Contact(@DefaultValue("SivaLabs") String name, @DefaultValue("support@sivalabs.in") String email) {}
}
