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
package com.axelixlabs.axelix.sbs.spring.core.log;

import org.jspecify.annotations.Nullable;

/**
 * Port interface (from Ports-And-Adapters) for the abstract logger. This interface is here to abstract away
 * different versions of SLF4J that may be used in different version of the Spring Boot.
 * <p>
 * We need this port since, again, Spring Boot 4 may (and it does AFAIR) use SLF4J 2, but Spring Boot 2 uses
 * SLF4J 1. So we have to abstract it away.
 *
 * @author Mikhail Polivakha
 */
public interface Logger {

    /**
     * Trace-log message
     */
    void trace(String message, @Nullable Object @Nullable ... args);

    /**
     * Info-log message
     */
    void info(String message, @Nullable Object @Nullable ... args);

    /**
     * Debug-log message
     */
    void debug(String message, @Nullable Object @Nullable ... args);
}
