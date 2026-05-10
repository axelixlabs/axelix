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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;

import java.util.Collection;

/**
 * Extension of {@link CacheManager} that exposes management operations on top of an existing
 * {@link CacheManager}. Implementations are typically attached to user-defined {@link CacheManager}
 * beans through an AOP proxy so that the original concrete type is preserved.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public interface EnhancedCacheManager extends CacheManager {

    /**
     * @return the bean name of the underlying {@link CacheManager} that this enhanced manager wraps.
     */
    String getUnderlyingCacheManagerBeanName();

    @Override
    @Nullable
    EnhancedCache getCache(@NonNull String name);

    /**
     * @return all enhanced caches that have been resolved through this manager.
     */
    Collection<EnhancedCache> getAll();

    void clear(String cacheName);

    void clear(String cacheName, Object key);

    void clearAll();

    void enable(String cacheName);

    void disable(String cacheName);

    void enableAll();

    void disableAll();

    boolean isEnabled(String cacheName);
}
