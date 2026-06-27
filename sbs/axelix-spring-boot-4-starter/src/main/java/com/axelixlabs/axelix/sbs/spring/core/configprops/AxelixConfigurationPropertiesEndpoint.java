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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;

/**
 * Custom Actuator endpoint exposing the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axelix-configprops")
public class AxelixConfigurationPropertiesEndpoint {

    private final ConfigurationPropertiesService configurationPropertiesService;

    public AxelixConfigurationPropertiesEndpoint(ConfigurationPropertiesService configurationPropertiesService) {
        this.configurationPropertiesService = configurationPropertiesService;
    }

    @ReadOperation
    public ConfigurationPropertiesFeed configurationProperties() {
        return configurationPropertiesService.getConfigProps();
    }
}
