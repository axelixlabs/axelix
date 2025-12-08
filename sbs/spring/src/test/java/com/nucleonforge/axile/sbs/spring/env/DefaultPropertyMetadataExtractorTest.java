/*
 * Copyright 2025-present the original author or authors.
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
        assertThat(serverPortMetadata.deprecation()).isNotNull();
        assertThat(serverPortMetadata.deprecation().reason()).isEqualTo("Just because");
        assertThat(serverPortMetadata.deprecation().replacement()).isEqualTo("new.prop.test.server.port");

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
