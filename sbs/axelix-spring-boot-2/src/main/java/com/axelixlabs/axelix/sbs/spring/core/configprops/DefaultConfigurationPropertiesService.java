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
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties;
import org.springframework.context.ApplicationContext;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;

/**
 * Default implementation of {@link ConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class DefaultConfigurationPropertiesService implements ConfigurationPropertiesService {

    private static final DefaultAuthority FULL_ACCESS_AUTHORITY = DefaultAuthority.CONFIG_PROPS_VALUES_READ;

    private final ConfigurationPropertiesConverter configurationPropertiesConverter;
    private final ConfigurationPropertiesReportEndpoint delegate;
    private final ConfigurationPropertiesReportEndpoint sanitizedDelegate;
    private final RequiredAuthorityCheckService requiredAuthorityCheckService;

    public DefaultConfigurationPropertiesService(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
        this.configurationPropertiesConverter = configurationPropertiesConverter;
        this.requiredAuthorityCheckService = requiredAuthorityCheckService;
        this.delegate = new ConfigurationPropertiesReportEndpoint(List.of());
        this.delegate.setApplicationContext(applicationContext);
        this.sanitizedDelegate = new ConfigurationPropertiesReportEndpoint(List.of(smartSanitizingFunction));
        this.sanitizedDelegate.setApplicationContext(applicationContext);

        // Spring Boot 2.7 has default sanitization keys (password, secret, key, token, etc.).
        // management.endpoint.env.keys-to-sanitize property only ADDS to this list, never replaces it.
        // To rely purely on our SmartSanitizingFunction and ensure identical behavior for all delegates
        // and across all versions of our starter, we explicitly clear the default list with setKeysToSanitize().
        this.delegate.setKeysToSanitize();
        this.sanitizedDelegate.setKeysToSanitize();
    }

    public ConfigurationPropertiesFeed getConfigProps() {
        boolean hasAuthority = requiredAuthorityCheckService.hasAuthority(FULL_ACCESS_AUTHORITY);

        ApplicationConfigurationProperties originalDescriptor;

        if (hasAuthority) {
            originalDescriptor = delegate.configurationProperties();
        } else {
            originalDescriptor = sanitizedDelegate.configurationProperties();
        }

        return configurationPropertiesConverter.convert(originalDescriptor);
    }
}
