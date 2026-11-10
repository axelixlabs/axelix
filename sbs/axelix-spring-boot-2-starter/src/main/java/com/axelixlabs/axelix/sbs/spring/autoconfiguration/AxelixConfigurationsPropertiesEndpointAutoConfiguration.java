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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.annotations.AxelixAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.PropertySensitivityMarker;

/**
 * Auto-configuration for the {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Ilya Naumov
 */
@AxelixAutoConfiguration(after = EndpointPropertiesSupportAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = AxelixConfigurationPropertiesEndpoint.class)
public class AxelixConfigurationsPropertiesEndpointAutoConfiguration {

    @Bean
    public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
        return new DefaultConfigurationPropertiesFlattener();
    }

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter(
            ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
        return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
    }

    @Bean
    public ConfigurationPropertiesService configurationPropertiesService(
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            PropertySensitivityMarker propertySensitivityMarker) {

        return new DefaultConfigurationPropertiesService(
                applicationContext, configurationPropertiesConverter, propertySensitivityMarker);
    }

    @Bean
    public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesService configurationPropertiesService) {
        return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesService);
    }
}
