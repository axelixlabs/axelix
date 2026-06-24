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

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

/**
 * Default implementation of {@link ServiceMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultServiceMetadataAssembler implements ServiceMetadataAssembler {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final HealthDetectionFunction healthDetectionFunction;
    private final GitInformationProvider gitInformationProvider;
    private final ShortBuildInfoProvider shortBuildInfoProvider;
    private final AxelixVersionDiscoverer axelixVersionDiscoverer;
    private final LibraryInformationProvider libraryInformationProvider;
    private final InsightsInfoProvider insightsInfoProvider;

    public DefaultServiceMetadataAssembler(
            HealthDetectionFunction healthDetectionFunction,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            GitInformationProvider gitInformationProvider,
            ShortBuildInfoProvider shortBuildInfoProvider,
            LibraryInformationProvider libraryInformationProvider,
            InsightsInfoProvider insightsInfoProvider) {

        this.healthDetectionFunction = healthDetectionFunction;
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
        this.gitInformationProvider = gitInformationProvider;
        this.shortBuildInfoProvider = shortBuildInfoProvider;
        this.libraryInformationProvider = libraryInformationProvider;
        this.insightsInfoProvider = insightsInfoProvider;
    }

    @Override
    public BasicDiscoveryMetadata assemble() {
        var gitCommitInfo = gitInformationProvider.getGitCommitInfo();

        return new BasicDiscoveryMetadata(
                axelixVersionDiscoverer.getVersion(),
                shortBuildInfoProvider.getShortBuildInfo().serviceVersion(),
                gitCommitInfo.commitShaShort(),
                libraryInformationProvider.getJdkVendorName(),
                buildSoftwareVersionInUse(),
                healthDetectionFunction.get(),
                new BasicDiscoveryMetadata.MemoryDetails(
                        memoryMXBean.getHeapMemoryUsage().getUsed()),
                insightsInfoProvider.getInsight());
    }

    private BasicDiscoveryMetadata.SoftwareVersions buildSoftwareVersionInUse() {
        return new BasicDiscoveryMetadata.SoftwareVersions(
                libraryInformationProvider.getJavaVersion(),
                libraryInformationProvider.getSpringBootVersion(),
                libraryInformationProvider.getSpringVersion(),
                libraryInformationProvider.getKotlinVersion());
    }
}
