package com.nucleonforge.axile.master.auth.spi.provider;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.nucleonforge.axile.master.Main;

/**
 * Base abstract class for integration tests using Testcontainers with a PostgreSQL database.
 *
 * @since 17.07.2025
 * @author Nikita Kirillov
 */
@Testcontainers
@SpringBootTest(classes = Main.class)
public abstract class BaseTestcontainersIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:16.4";
    private static final String POSTGRES_DB_NAME = "auth_db";
    private static final String POSTGRES_USERNAME = "postgres";
    private static final String POSTGRES_PASSWORD = "postgres";

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(POSTGRES_DB_NAME)
            .withUsername(POSTGRES_USERNAME)
            .withPassword(POSTGRES_PASSWORD);

    @DynamicPropertySource
    static void initProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
}
