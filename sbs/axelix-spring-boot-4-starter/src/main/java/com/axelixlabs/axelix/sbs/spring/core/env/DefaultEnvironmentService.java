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

import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.sbs.spring.core.configprops.PropertySensitivityMarker;

/**
 * Default implementation of {@link EnvironmentService}
 *
 * @author Nikita Kirillov
 */
public class DefaultEnvironmentService implements EnvironmentService {

    private final EnvironmentEndpoint delegate;
    private final EnvPropertyEnricher envPropertyEnricher;
    private final PropertySensitivityMarker propertySensitivityMarker;

    public DefaultEnvironmentService(
            Environment environment,
            EnvPropertyEnricher envPropertyEnricher,
            PropertySensitivityMarker propertySensitivityMarker) {
        this.envPropertyEnricher = envPropertyEnricher;
        this.delegate = new EnvironmentEndpoint(environment, List.of(), Show.ALWAYS);
        this.propertySensitivityMarker = propertySensitivityMarker;
    }

    public EnvironmentFeed getEnvironmentFeed(@Nullable String pattern) {
        return propertySensitivityMarker.mark(envPropertyEnricher.enrich(delegate.environment(pattern)));
    }
}
