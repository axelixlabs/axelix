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

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;

/**
 * Service for retrieving EnvironmentFeed.
 *
 * @author Nikita Kirillov
 */
public interface EnvironmentService {

    /**
     * Retrieves environment properties filtered by the specified pattern.
     *
     * @param pattern optional pattern to filter property names
     * @return environment feed containing matched properties with their values
     */
    EnvironmentFeed getEnvironmentFeed(@Nullable String pattern);
}
