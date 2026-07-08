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
package com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate;

import com.axelixlabs.axelix.sbs.spring.core.persistence.ProxyingPreparedStatement;

/**
 * Holds a thread-local flag indicating that the current query is subject to in-memory pagination.
 * The flag is set by InMemoryPaginationAppender upon detecting a Hibernate warning,
 * and cleared by {@link ProxyingPreparedStatement} after the query is recorded.
 *
 * @author Nikita Kirillov
 */
public final class InMemoryPaginationHolder {

    private static final ThreadLocal<Boolean> HOLDER = new ThreadLocal<>();

    private InMemoryPaginationHolder() {}

    public static void mark() {
        HOLDER.set(Boolean.TRUE);
    }

    public static boolean isMarked() {
        return Boolean.TRUE.equals(HOLDER.get());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
