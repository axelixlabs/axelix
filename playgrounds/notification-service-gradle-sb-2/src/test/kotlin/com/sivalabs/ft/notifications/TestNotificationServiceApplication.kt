package com.sivalabs.ft.notifications

import org.springframework.boot.builder.SpringApplicationBuilder
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

fun main(args: Array<String>) {
    val kafka = KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))
    kafka.start()

    SpringApplicationBuilder(NotificationServiceApplication::class.java)
        .properties(
            mapOf(
                "spring.kafka.bootstrap-servers" to kafka.bootstrapServers,
            ),
        ).run(*args)
}
