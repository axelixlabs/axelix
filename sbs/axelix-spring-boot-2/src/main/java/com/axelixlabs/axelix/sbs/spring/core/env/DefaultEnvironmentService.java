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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;

/**
 * Default implementation of {@link EnvironmentService}
 *
 * @author Nikita Kirillov
 */
public class DefaultEnvironmentService implements EnvironmentService {

    private static final DefaultAuthority FULL_ACCESS_AUTHORITY = DefaultAuthority.ENV_VALUES_READ;

    private final EnvironmentEndpoint delegate;
    private final EnvironmentEndpoint sanitizedDelegate;
    private final EnvPropertyEnricher envPropertyEnricher;
    private final RequiredAuthorityCheckService requiredAuthorityCheckService;

    public DefaultEnvironmentService(
            Environment environment,
            SmartSanitizingFunction smartSanitizingFunction,
            EnvPropertyEnricher envPropertyEnricher,
            RequiredAuthorityCheckService requiredAuthorityCheckService) {
        this.envPropertyEnricher = envPropertyEnricher;
        this.delegate = new EnvironmentEndpoint(environment, List.of());
        this.sanitizedDelegate = new EnvironmentEndpoint(environment, List.of(smartSanitizingFunction));
        this.requiredAuthorityCheckService = requiredAuthorityCheckService;

        // Spring Boot 2.7 has default sanitization keys (password, secret, key, token, etc.)
        // management.endpoint.env.keys-to-sanitize property only ADDS to this list, never replaces it.
        // To rely purely on our SmartSanitizingFunction and ensure identical behavior for both
        // delegates, we explicitly clear the default list with setKeysToSanitize().
        this.delegate.setKeysToSanitize();
        this.sanitizedDelegate.setKeysToSanitize();
    }

    public EnvironmentFeed getEnvironmentFeed(@Nullable String pattern) {
        boolean hasAuthority = requiredAuthorityCheckService.hasAuthority(FULL_ACCESS_AUTHORITY);

        EnvironmentDescriptor originalDescriptor;

        if (hasAuthority) {
            originalDescriptor = delegate.environment(pattern);
        } else {
            originalDescriptor = sanitizedDelegate.environment(pattern);
        }

        return envPropertyEnricher.enrich(originalDescriptor);
    }
}
