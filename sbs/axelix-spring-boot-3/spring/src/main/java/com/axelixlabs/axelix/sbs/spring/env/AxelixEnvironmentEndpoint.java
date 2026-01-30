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
package com.axelixlabs.axelix.sbs.spring.env;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.sbs.spring.configprops.SmartSanitizingFunction;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-env")
public class AxelixEnvironmentEndpoint {

    private final EnvironmentEndpoint delegate;
    private final EnvPropertyEnricher envPropertyEnricher;

    public AxelixEnvironmentEndpoint(
            Environment environment,
            SmartSanitizingFunction smartSanitizingFunction,
            EnvPropertyEnricher envPropertyEnricher) {
        this.delegate = new EnvironmentEndpoint(environment, List.of(smartSanitizingFunction), Show.ALWAYS);
        this.envPropertyEnricher = envPropertyEnricher;
    }

    @GetMapping
    public EnvironmentFeed environment(@Nullable String pattern) {
        EnvironmentDescriptor originalDescriptor = delegate.environment(pattern);

        return envPropertyEnricher.enrich(originalDescriptor);
    }
}
