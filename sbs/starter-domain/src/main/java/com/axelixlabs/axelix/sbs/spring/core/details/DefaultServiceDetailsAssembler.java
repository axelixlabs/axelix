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

import com.axelixlabs.axelix.sbs.spring.core.contract.details.BuildDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.CommitAuthor;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.GitDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.InstanceDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.OsDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.RuntimeDetails;
import com.axelixlabs.axelix.sbs.spring.core.contract.details.SpringDetails;
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

        return new InstanceDetails()
                .git(git)
                .spring(spring)
                .runtime(runtime)
                .build(build)
                .os(os);
    }

    private GitDetails getGitDetails() {
        CommitAuthor commitAuthor = new CommitAuthor()
                .name(axelixInfoProperties.getCommitUserName())
                .email(axelixInfoProperties.getCommitUserEmail());

        return new GitDetails()
                .commitShaShort(axelixInfoProperties.getCommitShaShort())
                .branch(axelixInfoProperties.getBranch())
                .commitAuthor(commitAuthor)
                .commitTimestamp(axelixInfoProperties.getCommitTimestamp());
    }

    private SpringDetails getSpringDetails() {
        return new SpringDetails()
                .springBootVersion(libraryInformationProvider.getSpringBootVersion())
                .springFrameworkVersion(libraryInformationProvider.getSpringVersion())
                .springCloudVersion(libraryInformationProvider.getSpringCloudVersion());
    }

    private RuntimeDetails getRuntimeDetails() {
        return new RuntimeDetails()
                .javaVersion(libraryInformationProvider.getJavaVersion())
                .jdkVendor(libraryInformationProvider.getJdkVendorName())
                .kotlinVersion(libraryInformationProvider.getKotlinVersion());
    }

    private BuildDetails getBuildDetails() {

        return new BuildDetails()
                .artifact(axelixInfoProperties.getArtifactId())
                .version(axelixInfoProperties.getServiceVersion())
                .group(axelixInfoProperties.getGroupId())
                .time(axelixInfoProperties.getBuildTimestamp());
    }

    private OsDetails getOsDetails() {
        return new OsDetails()
                .name(emptyIfNull(System.getProperty("os.name")))
                .version(emptyIfNull(System.getProperty("os.version")))
                .arch(emptyIfNull(System.getProperty("os.arch")));
    }
}
