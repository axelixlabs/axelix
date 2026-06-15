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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-env")
public class AxelixEnvironmentEndpoint {

    private final EnvironmentService environmentService;

    public AxelixEnvironmentEndpoint(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    public EnvironmentFeed environment(@Nullable String pattern) {
        return environmentService.getEnvironmentFeed(pattern);
    }
}
