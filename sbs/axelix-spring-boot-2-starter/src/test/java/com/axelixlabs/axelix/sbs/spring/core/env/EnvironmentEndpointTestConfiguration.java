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
 * Test configuration for {@link AxelixEnvironmentEndpointTest}, part of the shared endpoint test
 * context.
 *
 * <p>The {@link SmartSanitizingFunction} defined here is shared by both the environment and the
 * configuration properties endpoints, so its sanitization list is the union of the properties
 * sanitized by the respective tests.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class EnvironmentEndpointTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
    public EndpointsConfigurationProperties endpointsConfigurationProperties() {
        return new EndpointsConfigurationProperties();
    }

    @Bean
    public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(EnvironmentService environmentService) {
        return new AxelixEnvironmentEndpoint(environmentService);
    }

    @Bean
    public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                List.of(
                        "axelix.env.test.toBeSanitized",
                        "AXELIX_FOR_SANITIZATION",
                        "axelix.prop.test.tags.forSanitization",
                        "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
    }
}
