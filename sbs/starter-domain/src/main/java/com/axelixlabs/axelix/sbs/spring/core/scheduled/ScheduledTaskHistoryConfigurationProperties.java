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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.sbs.spring.core.config.Validatable;

/**
 * Configuration properties for scheduled tasks history.
 *
 * @author Vyacheslav Yanin
 */
public class ScheduledTaskHistoryConfigurationProperties implements Validatable {

    public static final String CONFIG_PROPS_PREFIX = "axelix.sbs.scheduling";

    /**
     * The value of the queue limit
     */
    private int historyMaxSize = 30;

    public int getHistoryMaxSize() {
        return historyMaxSize;
    }

    public void setHistoryMaxSize(int historyMaxSize) {
        this.historyMaxSize = historyMaxSize;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        Assert.isTrue(historyMaxSize > 0, "historyMaxSize must be positive");
    }
}
