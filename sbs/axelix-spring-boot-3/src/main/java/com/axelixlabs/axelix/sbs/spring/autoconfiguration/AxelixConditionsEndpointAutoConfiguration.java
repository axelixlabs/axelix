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

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.conditions.AxelixConditionsEndpoint;

/**
 * Auto-configuration for the {@link AxelixConditionsEndpoint}.
 *
 * @since 20.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixConditionsEndpoint.class)
public class AxelixConditionsEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixConditionsEndpoint axelixConditionsEndpoint(
            ConfigurableApplicationContext configurableApplicationContext) {
        return new AxelixConditionsEndpoint(configurableApplicationContext);
    }
}
