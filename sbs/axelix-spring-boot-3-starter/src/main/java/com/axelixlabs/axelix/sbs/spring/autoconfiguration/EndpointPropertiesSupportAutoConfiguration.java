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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

/**
 * Auto-configuration for shared endpoint properties support beans.
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration(after = {SecurityContextExecutorAutoConfiguration.class})
public class EndpointPropertiesSupportAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
    public EndpointsConfigurationProperties endpointsConfigurationProperties() {
        return new EndpointsConfigurationProperties();
    }

    @Bean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public SmartSanitizingFunction smartSanitizingFunction(
            EndpointsConfigurationProperties endpointsConfigurationProperties,
            PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                endpointsConfigurationProperties.getSanitizedProperties(), propertyNameNormalizer);
    }

    @Bean
    public RequiredAuthorityCheckService requiredAuthorityCheckService(
            SecurityContextExecutor securityContextExecutor) {
        return new RequiredAuthorityCheckService(securityContextExecutor);
    }
}
