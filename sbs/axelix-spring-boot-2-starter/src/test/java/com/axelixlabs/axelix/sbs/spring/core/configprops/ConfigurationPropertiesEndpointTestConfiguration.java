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

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for {@link AxelixConfigurationPropertiesEndpointTest}, part of the shared
 * endpoint test context. The supporting beans (flattener, converter, service, etc.) come from
 * {@link com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@TestConfiguration
public class ConfigurationPropertiesEndpointTestConfiguration {

    @Bean
    public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesService configurationPropertiesService) {
        return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesService);
    }
}
