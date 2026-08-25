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
    public static final int EXCEPTION_HANDLING_FILTER = Ordered.HIGHEST_PRECEDENCE + 6;
    public static final int SPA_STATIC_RESOURCES_SERVING_FILTER = Ordered.HIGHEST_PRECEDENCE + 7;
    public static final int EXTERNAL_AUTHENTICATION_API_FILTER = Ordered.HIGHEST_PRECEDENCE + 8;
    public static final int EXTERNAL_API_JWT_AUTHORIZATION_FILTER = Ordered.HIGHEST_PRECEDENCE + 9;
    public static final int HEART_BEAT_JWT_AUTHORIZATION_FILTER = Ordered.HIGHEST_PRECEDENCE + 10;
    public static final int MCP_AUTHORIZATION_FILTER = Ordered.HIGHEST_PRECEDENCE + 11;
}
