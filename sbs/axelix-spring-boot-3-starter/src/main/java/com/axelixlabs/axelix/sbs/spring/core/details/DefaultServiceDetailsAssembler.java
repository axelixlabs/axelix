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
package com.axelixlabs.axelix.sbs.spring.core.details;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails.CommitAuthor;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.sbs.spring.core.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;

import static com.axelixlabs.axelix.sbs.spring.core.details.GarbageCollectorInfoAssembler.getGarbageCollectorInfo;
import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Default implementation of {@link ServiceDetailsAssembler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
public class DefaultServiceDetailsAssembler implements ServiceDetailsAssembler {

    private static final Logger log = LoggerFactory.getLogger(DefaultServiceDetailsAssembler.class);

    private final GitInformationProvider gitInformationProvider;
    private final @Nullable BuildProperties buildProperties;
    private final LibraryInformationProvider libraryInformationProvider;

    public DefaultServiceDetailsAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryInformationProvider libraryInformationProvider) {
        this.gitInformationProvider = gitInformationProvider;
        this.buildProperties = providerBuildProperties.getIfAvailable();
        this.libraryInformationProvider = libraryInformationProvider;
    }

    @Override
    public InstanceDetails assemble() {
        GitDetails git = getGitDetails();
        SpringDetails spring = getSpringDetails();
        RuntimeDetails runtime = getRuntimeDetails();
        BuildDetails build = getBuildDetails();
        OsDetails os = getOsDetails();

        return new InstanceDetails(git, spring, runtime, build, os);
    }

    private GitDetails getGitDetails() {
        GitInfo gitCommitInfo = gitInformationProvider.getGitCommitInfo();
        GitInfo.CommitAuthor commitAuthor = gitCommitInfo.commitAuthor();

        return new GitDetails(
                emptyIfNull(gitCommitInfo.commitShaShort()),
                emptyIfNull(gitCommitInfo.branch()),
                new CommitAuthor(emptyIfNull(commitAuthor.name()), emptyIfNull(commitAuthor.email())),
                gitCommitInfo.commitTimestamp());
    }

    private SpringDetails getSpringDetails() {
        return new SpringDetails(
                libraryInformationProvider.getSpringBootVersion(),
                libraryInformationProvider.getSpringVersion(),
                libraryInformationProvider.getSpringCloudVersion());
    }

    private RuntimeDetails getRuntimeDetails() {
        return new RuntimeDetails(
                libraryInformationProvider.getJavaVersion(),
                libraryInformationProvider.getJdkVendorName(),
                getGarbageCollectorInfo(),
                libraryInformationProvider.getKotlinVersion());
    }

    private BuildDetails getBuildDetails() {
        if (buildProperties == null) {
            return new BuildDetails("", "", "", "");
        }

        return new BuildDetails(
                emptyIfNull(buildProperties.getArtifact()),
                emptyIfNull(buildProperties.getVersion()),
                emptyIfNull(buildProperties.getGroup()),
                emptyIfNull(buildProperties.getTime().toString()));
    }

    private OsDetails getOsDetails() {
        return new OsDetails(
                emptyIfNull(System.getProperty("os.name")),
                emptyIfNull(System.getProperty("os.version")),
                emptyIfNull(System.getProperty("os.arch")));
    }

    private String getGarbageCollectorInfo() {

        String garbageCollectorInfo = GarbageCollectorInfoAssembler.getGarbageCollectorInfo();
        if (garbageCollectorInfo.equals(GarageCollector.UNKNOWN.name())) {
            log.warn("Unable to determine the GC used inside the given application. Falling back to UNKNOWN");
        }

        return garbageCollectorInfo;
    }
}
