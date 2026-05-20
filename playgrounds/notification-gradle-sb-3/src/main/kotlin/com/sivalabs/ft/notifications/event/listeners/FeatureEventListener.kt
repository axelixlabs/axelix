package com.sivalabs.ft.notifications.event.listeners

import com.sivalabs.ft.features.domain.FeatureCreatedEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class FeatureEventListener {
    @KafkaListener(topics = ["\${ft.events.new-features}"])
    fun onFeatureCreatedEvent(event: FeatureCreatedEvent) {
        println("Received FeatureCreatedEvent: $event")
    }
}
