package com.sivalabs.ft.features.domain

import java.time.Instant

class FeatureCreatedEvent(
    val id: Long,
    val code: String,
    val title: String,
    val description: String?,
    val status: FeatureStatus,
    val assignedTo: String?,
    val createdBy: String,
    val createdAt: Instant,
)
