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
package com.axelixlabs.axelix.sbs.spring.gclog;

import java.io.File;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * Service for managing JVM GC logging at runtime.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
public interface GcLogService {

    /**
     * Returns the current GC logging status.
     *
     * @return current GC logging status
     * @throws GcLogException if the GC logging status cannot be reliably determined
     */
    GcLogStatusResponse getStatus() throws GcLogException;

    /**
     * Returns a {@link File} pointing to the JVM GC log.
     *
     * <p>The file may not exist yet. Throws {@link GcLogException} if the file path cannot be resolved.
     *
     * @return GC log file reference
     * @throws GcLogException if the GC log file path cannot be determined
     */
    File getGcLogFile() throws GcLogException;

    /**
     * Enables GC logging with the given log level.
     *
     * @param level GC log level to enable
     * @throws GcLogException if the level is not supported, or an error occurred enabling logging.
     */
    void enable(String level) throws GcLogException;

    /**
     * Disables GC logging.
     *
     * @throws GcLogException if error occurred disabling logging.
     */
    void disable() throws GcLogException;
}
