package com.sivalabs.ft.notifications

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "ft")
@Validated
data class ApplicationProperties(
    val events: EventProperties,
) {
    data class EventProperties(
        val newFeatures: String,
        val updatedFeatures: String,
        val deletedFeatures: String,
    )
}
