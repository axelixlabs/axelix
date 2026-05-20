package com.sivalabs.ft.features;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
@Profile("posrtgres")
public class TestcontainersConfiguration {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return kafka;
    }
}
