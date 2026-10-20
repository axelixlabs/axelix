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
package com.axelixlabs.axelix.sbs.spring.core.config;

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * Configuration properties for the in-memory log buffer.
 *
 * @author Vyacheslav Yanin
 */
public class LogsConfigurationProperties implements Validatable {

    public static final String CONFIG_PROPS_PREFIX = "axelix.sbs.logs";

    /**
     * The maximum amount of the most recent log entries kept in memory.
     */
    private Integer maxEntries;

    public LogsConfigurationProperties() {
        this.maxEntries = 1000;
    }

    public Integer getMaxEntries() {
        return maxEntries;
    }

    public LogsConfigurationProperties setMaxEntries(Integer maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        Assert.isTrue(maxEntries > 0, "axelix.sbs.logs.max-entries must be positive");
    }
}
