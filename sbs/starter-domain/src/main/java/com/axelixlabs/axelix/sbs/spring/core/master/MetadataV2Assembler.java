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

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.MemoryDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadatav2.MetadataV2;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

/**
 * Assembles the wire {@link MetadataV2}.
 *
 * @author Nikita Kirillov
 */
public class MetadataV2Assembler {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final HealthDetectionFunction healthDetectionFunction;
    private final InsightsInfoProvider insightsInfoProvider;

    public MetadataV2Assembler(
            HealthDetectionFunction healthDetectionFunction, InsightsInfoProvider insightsInfoProvider) {
        this.healthDetectionFunction = healthDetectionFunction;
        this.insightsInfoProvider = insightsInfoProvider;
    }

    public MetadataV2 assemble() {
        return new MetadataV2()
                .healthStatus(healthDetectionFunction.get())
                .memoryDetails(new MemoryDetails()
                        .heap(memoryMXBean.getHeapMemoryUsage().getUsed()))
                .transactions(insightsInfoProvider.getTransactions());
    }
}
