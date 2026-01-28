/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.env;

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
