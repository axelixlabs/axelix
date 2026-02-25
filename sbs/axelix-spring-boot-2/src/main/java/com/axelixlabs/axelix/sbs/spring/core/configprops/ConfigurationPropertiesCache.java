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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.context.ApplicationContext;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;

/**
 * Service caching the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class ConfigurationPropertiesCache {

    private final ConfigurationPropertiesReportEndpoint delegate;
    private final ConfigurationPropertiesConverter configurationPropertiesConverter;

    @Nullable
    private volatile ConfigurationPropertiesFeed cachedResult;

    public ConfigurationPropertiesCache(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        // ALWAYS is required here in order for Spring Boot to invoke our custom sanitization function
        this.delegate = new ConfigurationPropertiesReportEndpoint(List.of(smartSanitizingFunction));
        this.delegate.setApplicationContext(applicationContext);
        this.configurationPropertiesConverter = configurationPropertiesConverter;
    }

    public ConfigurationPropertiesFeed getConfigProps() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = configurationPropertiesConverter.convert(delegate.configurationProperties());
                }
            }
        }
        return cachedResult;
    }
}
