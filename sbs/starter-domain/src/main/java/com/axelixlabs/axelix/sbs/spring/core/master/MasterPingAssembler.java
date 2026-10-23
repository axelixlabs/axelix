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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import com.axelixlabs.axelix.sbs.spring.core.contract.masterping.MasterPing;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.MemoryDetails;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

/**
 * Assembles the wire {@link MasterPing}.
 *
 * @author Nikita Kirillov
 */
public class MasterPingAssembler {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final HealthDetectionFunction healthDetectionFunction;
    private final InsightsInfoProvider insightsInfoProvider;

    public MasterPingAssembler(
            HealthDetectionFunction healthDetectionFunction, InsightsInfoProvider insightsInfoProvider) {
        this.healthDetectionFunction = healthDetectionFunction;
        this.insightsInfoProvider = insightsInfoProvider;
    }

    public MasterPing assemble() {
        return new MasterPing()
                .healthStatus(healthDetectionFunction.get())
                .memoryDetails(new MemoryDetails()
                        .heap(memoryMXBean.getHeapMemoryUsage().getUsed()))
                .transactions(insightsInfoProvider.getTransactions());
    }
}
