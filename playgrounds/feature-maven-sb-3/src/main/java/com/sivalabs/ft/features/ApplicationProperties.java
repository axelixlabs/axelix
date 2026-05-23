package com.sivalabs.ft.features;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ft")
public record ApplicationProperties(EventsProperties events) {

    public record EventsProperties(String newFeatures, String updatedFeatures, String deletedFeatures) {}
}
