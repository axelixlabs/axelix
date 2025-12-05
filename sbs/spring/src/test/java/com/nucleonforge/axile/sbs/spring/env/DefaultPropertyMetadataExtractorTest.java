package com.nucleonforge.axile.sbs.spring.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultPropertyMetadataExtractor}
 *
 * @author Nikita Kirillov
 * @since 05.12.2025
 */
@TestPropertySource(
        properties = {
            "prop.test.server.port=test",
            "prop.test.logging.level.root=test",
            "additional.custom.property=test"
        })
@SpringBootTest
@Import(DefaultPropertyMetadataExtractorTest.DefaultPropertyMetadataExtractorTestConfiguration.class)
class DefaultPropertyMetadataExtractorTest {

    @Autowired
    private PropertyMetadataExtractor extractor;

    @Autowired
    private PropertyNameNormalizer normalizer;

    @BeforeEach
    void setUp() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    void shouldExtractAllPropertyMetadataCorrectly() {
        PropertyMetadata serverPortMetadata = extractor.getMetadata(normalizer.normalize("prop.test.server.port"));
        assertThat(serverPortMetadata).isNotNull();
        assertThat(serverPortMetadata.description()).isEqualTo("Server HTTP port.");
        assertThat(serverPortMetadata.deprecated()).isTrue();
        assertThat(serverPortMetadata.deprecatedReason()).isEqualTo("Just because");
        assertThat(serverPortMetadata.deprecatedReplacement()).isEqualTo("new.prop.test.server.port");

        PropertyMetadata loggingMetadata = extractor.getMetadata(normalizer.normalize("prop.test.logging.level.root"));
        assertThat(loggingMetadata).isNotNull();
        assertThat(loggingMetadata.description()).isEqualTo("Logging level for root logger.");

        PropertyMetadata nonExistentMetadata = extractor.getMetadata("non.existent.property");
        assertThat(nonExistentMetadata).isNull();
    }

    @TestConfiguration
    static class DefaultPropertyMetadataExtractorTestConfiguration {

        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public PropertyMetadataExtractor propertyMetadataExtractor(
                ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
            return new DefaultPropertyMetadataExtractor(configurableEnvironment, propertyNameNormalizer);
        }
    }
}
