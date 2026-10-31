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
package com.axelixlabs.axelix.master.filter;

import org.springframework.core.Ordered;

/**
 * Class that holds ordering of the HTTP servlet filters.
 *
 * @author Mikhail Polivakha
 */
public final class FiltersOrder {

    private FiltersOrder() {}

    public static final int REQUEST_PROFILE_FILTER = Ordered.HIGHEST_PRECEDENCE + 5;

    public static final int EXCEPTION_HANDLING_FILTER = REQUEST_PROFILE_FILTER + 1;

    public static final int SPA_STATIC_RESOURCES_SERVING_FILTER = EXCEPTION_HANDLING_FILTER + 1;

    public static final int EXTERNAL_AUTHENTICATION_API_FILTER = SPA_STATIC_RESOURCES_SERVING_FILTER + 1;

    public static final int EXTERNAL_API_JWT_AUTHORIZATION_FILTER = EXTERNAL_AUTHENTICATION_API_FILTER + 1;

    public static final int HEART_BEAT_JWT_AUTHORIZATION_FILTER = EXTERNAL_API_JWT_AUTHORIZATION_FILTER + 1;

    public static final int MCP_AUTHORIZATION_FILTER = HEART_BEAT_JWT_AUTHORIZATION_FILTER + 1;
}
