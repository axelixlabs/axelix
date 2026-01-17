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
package com.nucleonforge.axelix.common.domain.spring;

import java.util.Set;

import com.nucleonforge.axelix.common.domain.LoadedClass;

public class SpringCacheManager {

    /**
     * The name of this Cache Manager
     */
    private String name;

    /**
     * Caches that this Spring's {@code CacheManager} manages
     */
    private Set<String> caches;

    /**
     * The information of the {@link java.lang.Class} from which the given CacheManager was created
     */
    private LoadedClass classInfo;

    public SpringCacheManager(String name, Set<String> caches, LoadedClass classInfo) {
        this.name = name;
        this.caches = caches;
        this.classInfo = classInfo;
    }
}
