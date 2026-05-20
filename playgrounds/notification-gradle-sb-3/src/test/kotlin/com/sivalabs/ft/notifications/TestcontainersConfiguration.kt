package com.sivalabs.ft.notifications

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))
}
