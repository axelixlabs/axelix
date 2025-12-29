/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.env;

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
            "custom.test.without.reason.property=test",
            "custom.test.without.replacement.property=test"
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
        assertThat(serverPortMetadata.deprecation()).isNotNull();
        assertThat(serverPortMetadata.deprecation().message())
                .isEqualTo("Just because. Deprecated in favor of new.prop.test.server.port property.");
    }

    @Test
    void shouldExtractPropertyMetadataWithoutReason() {
        PropertyMetadata metadataWithoutReason =
                extractor.getMetadata(normalizer.normalize("custom.test.without.replacement.property"));
        assertThat(metadataWithoutReason).isNotNull();
        assertThat(metadataWithoutReason.description()).isNull();
        assertThat(metadataWithoutReason.deprecation()).isNotNull();
        assertThat(metadataWithoutReason.deprecation().message()).isEqualTo("Marked for deletion.");
    }

    @Test
    void shouldExtractPropertyMetadataWithoutReplacement() {
        PropertyMetadata metadataWithoutReplacament =
                extractor.getMetadata(normalizer.normalize("custom.test.without.reason.property"));
        assertThat(metadataWithoutReplacament).isNotNull();
        assertThat(metadataWithoutReplacament.description()).isNull();
        assertThat(metadataWithoutReplacament.deprecation()).isNotNull();
        assertThat(metadataWithoutReplacament.deprecation().message())
                .isEqualTo("Deprecated in favor of new.custom.test.without.reason.property property.");
    }

    @Test
    void shouldExtractPropertyMetadataWithoutDeprecated() {
        PropertyMetadata loggingMetadata = extractor.getMetadata(normalizer.normalize("prop.test.logging.level.root"));
        assertThat(loggingMetadata).isNotNull();
        assertThat(loggingMetadata.description()).isEqualTo("Logging level for root logger.");
        assertThat(loggingMetadata.deprecation()).isNull();
    }

    @Test
    void shouldNotExtractPropertyMetadataWithoutDeprecated() {
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
