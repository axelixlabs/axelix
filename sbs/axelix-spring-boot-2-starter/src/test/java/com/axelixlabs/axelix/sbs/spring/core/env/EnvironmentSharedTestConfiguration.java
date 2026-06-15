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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;

/**
 * Test configuration for the shared application context used by the non-endpoint tests of the
 * {@code env} package, see {@link AbstractEnvironmentIntegrationTest}.
 *
 * <p>The {@link SmartSanitizingFunction} defined here sanitizes only the properties asserted by
 * {@code DefaultEnvironmentServiceTest.WithExplicitSanitizationProperties}; tests that require a
 * differently configured function construct their subject manually.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class EnvironmentSharedTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
    public EndpointsConfigurationProperties endpointsConfigurationProperties() {
        return new EndpointsConfigurationProperties();
    }

    @Bean
    public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
    }
}
