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

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.BuildDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.GitDetails.CommitAuthor;
import com.axelixlabs.axelix.common.api.InstanceDetails.OsDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.axelixlabs.axelix.common.api.InstanceDetails.SpringDetails;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;

import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Default implementation of {@link ServiceDetailsAssembler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
public class DefaultServiceDetailsAssembler implements ServiceDetailsAssembler {

    private final AxelixInfoProperties axelixInfoProperties;
    private final LibraryInformationProvider libraryInformationProvider;

    public DefaultServiceDetailsAssembler(
            AxelixInfoProperties axelixInfoProperties, LibraryInformationProvider libraryInformationProvider) {
        this.axelixInfoProperties = axelixInfoProperties;
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
        CommitAuthor commitAuthor =
                new CommitAuthor(axelixInfoProperties.getCommitUserName(), axelixInfoProperties.getCommitUserEmail());

        return new GitDetails(
                axelixInfoProperties.getCommitShaShort(),
                axelixInfoProperties.getBranch(),
                commitAuthor,
                axelixInfoProperties.getCommitTimestamp());
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
                libraryInformationProvider.getKotlinVersion());
    }

    private BuildDetails getBuildDetails() {

        return new BuildDetails(
                axelixInfoProperties.getArtifactId(),
                axelixInfoProperties.getServiceVersion(),
                axelixInfoProperties.getGroupId(),
                axelixInfoProperties.getBuildTimestamp());
    }

    private OsDetails getOsDetails() {
        return new OsDetails(
                emptyIfNull(System.getProperty("os.name")),
                emptyIfNull(System.getProperty("os.version")),
                emptyIfNull(System.getProperty("os.arch")));
    }
}
