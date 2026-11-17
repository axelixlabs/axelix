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
package com.axelixlabs.axelix.master.service.dashboard;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Names and construction of the dashboard {@link CacheManager}.
 *
 * <p>One manager holds a separate cache per dashboard. Each cache keeps a single entry: the result rendered on that
 * dashboard.
 *
 * @author Vyacheslav Yanin
 */
public final class DashboardCaches {

    public static final String PERSISTENCE = "persistence";

    public static final String DEPENDENCIES = "dependencies";

    public static final String SPRING_PORTFOLIO = "springPortfolio";

    public static final String LANGUAGES = "languages";

    private static final long MAXIMUM_SIZE = 1;

    private DashboardCaches() {}

    public static CacheManager cacheManager() {
        CaffeineCacheManager cacheManager =
                new CaffeineCacheManager(PERSISTENCE, DEPENDENCIES, SPRING_PORTFOLIO, LANGUAGES);
        cacheManager.setCaffeine(Caffeine.newBuilder().maximumSize(MAXIMUM_SIZE).executor(Runnable::run));
        return cacheManager;
    }
}
