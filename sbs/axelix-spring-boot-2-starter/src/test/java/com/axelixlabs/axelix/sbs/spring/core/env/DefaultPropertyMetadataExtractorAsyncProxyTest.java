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
package com.axelixlabs.axelix.sbs.spring.core.env;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for GH-1503: when the host application enables {@link EnableAsync @EnableAsync}, the
 * {@link DefaultPropertyMetadataExtractor} bean must still start. It implements an interface, so Spring uses a
 * JDK dynamic proxy that exposes only the interface methods; its package-private {@code @EventListener} method is
 * not part of that interface. Previously the method was also annotated with {@code @Async}, which made the bean a
 * proxy target and caused context startup to fail with {@code IllegalStateException: Need to invoke method
 * 'loadAndFilterPropertyMetadata' ... but not found in any interface(s) of the exposed proxy type}.
 *
 * @author Mikhail Polivakha
 */
class DefaultPropertyMetadataExtractorAsyncProxyTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(AsyncEnabledConfig.class);

    @Test
    void shouldStartContextWhenHostApplicationEnablesAsync() {
        // given / when / then
        contextRunner.run(context -> assertThat(context).hasNotFailed().hasSingleBean(PropertyMetadataExtractor.class));
    }

    @EnableAsync
    @Configuration
    static class AsyncEnabledConfig {

        @Bean
        PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        PropertyMetadataExtractor propertyMetadataExtractor(
                ConfigurableEnvironment environment, PropertyNameNormalizer propertyNameNormalizer) {
            return new DefaultPropertyMetadataExtractor(environment, propertyNameNormalizer);
        }
    }
}
