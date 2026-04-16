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
import java.util.Optional;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;

import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Default implementation of {@link ServiceMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultServiceMetadataAssembler implements ServiceMetadataAssembler {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final HealthDetectionFunction healthDetectionFunction;
    private final List<GitInformationProvider> gitInformationProvider;
    private final List<ShortBuildInfoProvider> shortBuildInfoProvider;
    private final AxelixVersionDiscoverer axelixVersionDiscoverer;
    private final LibraryDiscoverer libraryDiscoverer;
    private final VMFeaturesProvider vmFeaturesProvider;

    public DefaultServiceMetadataAssembler(
            HealthDetectionFunction healthDetectionFunction,
            LibraryDiscoverer libraryDiscoverer,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders,
            VMFeaturesProvider vmFeaturesProvider) {

        this.healthDetectionFunction = healthDetectionFunction;
        this.libraryDiscoverer = libraryDiscoverer;
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
        this.gitInformationProvider = gitInformationProviders;
        this.shortBuildInfoProvider = shortBuildInfoProviders;
        this.vmFeaturesProvider = vmFeaturesProvider;
    }

    @Override
    public BasicDiscoveryMetadata assemble() {
        var shortBuildInfo = getShortBuildInfo();
        var gitCommitInfo = getGitCommitInfo();

        return new BasicDiscoveryMetadata(
                axelixVersionDiscoverer.getVersion(),
                shortBuildInfo.map(ShortBuildInfo::serviceVersion).orElse(""),
                gitCommitInfo.map(GitInfo::commitShaShort).orElse(""),
                emptyIfNull(System.getProperty("java.vendor")),
                buildSoftwareVersionInUse(),
                healthDetectionFunction.get(),
                new BasicDiscoveryMetadata.MemoryDetails(
                        memoryMXBean.getHeapMemoryUsage().getUsed()),
                vmFeaturesProvider.discover());
    }

    private Optional<ShortBuildInfo> getShortBuildInfo() {
        for (var provider : shortBuildInfoProvider) {
            Optional<ShortBuildInfo> shortBuildInfo = provider.getShortBuildInfo();

            if (shortBuildInfo.isPresent()) {
                return shortBuildInfo;
            }
        }

        return Optional.empty();
    }

    private Optional<GitInfo> getGitCommitInfo() {
        for (var provider : gitInformationProvider) {
            Optional<GitInfo> gitCommitInfo = provider.getGitCommitInfo();

            if (gitCommitInfo.isPresent()) {
                return gitCommitInfo;
            }
        }

        return Optional.empty();
    }

    private BasicDiscoveryMetadata.SoftwareVersions buildSoftwareVersionInUse() {
        String javaVersion = emptyIfNull(System.getProperty("java.version"));
        var springBootVersion = libraryDiscoverer.getLibraryVersion("spring-boot", "org.springframework.boot");
        var springVersion = libraryDiscoverer.getLibraryVersion("spring-core", "org.springframework");
        var kotlinVersion = libraryDiscoverer.getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin");

        return new BasicDiscoveryMetadata.SoftwareVersions(
                javaVersion, springBootVersion.orElse(""), springVersion.orElse(""), kotlinVersion.orElse(null));
    }
}
