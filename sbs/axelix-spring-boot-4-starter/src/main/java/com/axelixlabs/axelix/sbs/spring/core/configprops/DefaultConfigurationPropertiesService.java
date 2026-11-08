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

import java.util.List;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.context.ApplicationContext;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;

/**
 * Default implementation of {@link ConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class DefaultConfigurationPropertiesService implements ConfigurationPropertiesService {

    private final ConfigurationPropertiesConverter configurationPropertiesConverter;
    private final ConfigurationPropertiesReportEndpoint delegate;

    public DefaultConfigurationPropertiesService(
            ApplicationContext applicationContext, ConfigurationPropertiesConverter configurationPropertiesConverter) {
        this.configurationPropertiesConverter = configurationPropertiesConverter;

        // ALWAYS is required here so that property values are included in the response.
        this.delegate = new ConfigurationPropertiesReportEndpoint(List.of(), Show.ALWAYS);
        this.delegate.setApplicationContext(applicationContext);
    }

    public ConfigurationPropertiesFeed getConfigProps() {
        return configurationPropertiesConverter.convert(delegate.configurationProperties());
    }
}
