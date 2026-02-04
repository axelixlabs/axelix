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
import java.util.List;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;
import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;

/**
 * Default implementation of {@link ServiceMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultServiceMetadataAssembler implements ServiceMetadataAssembler {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final HealthEndpoint healthEndpoint;
    private final GitInformationProvider gitInformationProvider;
    private final AxelixVersionDiscoverer axelixVersionDiscoverer;
    private final ShortBuildInfoProvider shortBuildInfoProvider;
    private final LibraryDiscoverer libraryDiscoverer;
    private final VMFeaturesProvider vmFeaturesProvider;

    @SuppressWarnings("NullAway")
    public DefaultServiceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            LibraryDiscoverer libraryDiscoverer,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders,
            VMFeaturesProvider vmFeaturesProvider) {

        this.healthEndpoint = healthEndpoint;
        this.libraryDiscoverer = libraryDiscoverer;
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
        this.gitInformationProvider = CollectionUtils.firstElement(gitInformationProviders);
        this.shortBuildInfoProvider = CollectionUtils.firstElement(shortBuildInfoProviders);
        this.vmFeaturesProvider = vmFeaturesProvider;

        Assert.notNull(this.healthEndpoint, "The HealthEndpoint must not be null");
        Assert.notNull(this.gitInformationProvider, missingGitInfoProvider());
        Assert.notNull(this.shortBuildInfoProvider, missingShortBuildInfoProvider());
    }

    @Override
    public BasicDiscoveryMetadata assemble() {
        var shortBuildInfo = shortBuildInfoProvider.getShortBuildInfo();
        var gitCommitInfo = gitInformationProvider.getGitCommitInfo();

        return new BasicDiscoveryMetadata(
                axelixVersionDiscoverer.getVersion(),
                shortBuildInfo.map(ShortBuildInfo::serviceVersion).orElse(""),
                gitCommitInfo.map(GitInfo::commitShaShort).orElse(""),
                System.getProperty("java.vendor"),
                buildSoftwareVersionInUse(),
                getCurrentHealth(),
                new BasicDiscoveryMetadata.MemoryDetails(
                        memoryMXBean.getHeapMemoryUsage().getUsed()),
                vmFeaturesProvider.discover());
    }

    private BasicDiscoveryMetadata.SoftwareVersions buildSoftwareVersionInUse() {
        return new BasicDiscoveryMetadata.SoftwareVersions(
                System.getProperty("java.version"),
                libraryDiscoverer.getRequiredLibraryVersion("spring-boot", "org.springframework.boot"),
                libraryDiscoverer.getRequiredLibraryVersion("spring-core", "org.springframework"),
                libraryDiscoverer
                        .getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin")
                        .orElse(null));
    }

    private BasicDiscoveryMetadata.HealthStatus getCurrentHealth() {
        Status status = healthEndpoint.health().getStatus();

        if (status == Status.UP) {
            return BasicDiscoveryMetadata.HealthStatus.UP;
        }

        if (status == Status.DOWN) {
            return BasicDiscoveryMetadata.HealthStatus.DOWN;
        }

        // defaulting to unknown in case of UNKNOWN, OUT_OF_SERVICE and custom statuses
        return BasicDiscoveryMetadata.HealthStatus.UNKNOWN;
    }

    private static String missingGitInfoProvider() {
        return """
            There is no GitInformationProvider available for this service.
            This means that there is no git.properties file in classpath (generated by git-commit-id plugin)
            """;
    }

    private static String missingShortBuildInfoProvider() {
        return """
            There is no ShortBuildInfoProvider available for this service.
            This means that there is no git.properties file in classpath (generated by git-commit-id plugin)
            """;
    }
}
