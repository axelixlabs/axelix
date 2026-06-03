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
package com.axelixlabs.axelix.sbs.spring.core.metrics;

/**
 * Utility class containing standardized metric name constants for the Axelix starter.
 *
 * @author Nikita Kirillov
 */
public final class AxelixMetricNames {

    public static final String TRANSACTION_DURATION = "axelix_transaction_duration";
    public static final String TRANSACTION_QUERIES = "axelix_transaction_queries";
    public static final String CACHE_REQUESTS = "axelix_cache_requests";
    public static final String CACHE_ENABLED = "axelix_cache_enabled";

    private AxelixMetricNames() {}
}
