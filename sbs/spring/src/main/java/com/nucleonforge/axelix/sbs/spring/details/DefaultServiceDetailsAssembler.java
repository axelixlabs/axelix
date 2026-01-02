/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.details;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import com.nucleonforge.axelix.common.api.InstanceDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.BuildDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.GitDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.GitDetails.CommitAuthor;
import com.nucleonforge.axelix.common.api.InstanceDetails.OsDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.RuntimeDetails;
import com.nucleonforge.axelix.common.api.InstanceDetails.SpringDetails;
import com.nucleonforge.axelix.common.api.registration.GitInfo;
import com.nucleonforge.axelix.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axelix.sbs.spring.master.LibraryDiscoverer;

import static com.nucleonforge.axelix.sbs.spring.details.GarbageCollectorInfoAssembler.getGarbageCollectorInfo;
import static com.nucleonforge.axelix.sbs.spring.utils.StringUtils.emptyIfNull;

// TODO: Revisit the design of this class.
/**
 * Default implementation of {@link ServiceDetailsAssembler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
public class DefaultServiceDetailsAssembler implements ServiceDetailsAssembler {
    private final GitInformationProvider gitInformationProvider;
    private final @Nullable BuildProperties buildProperties;
    private final LibraryDiscoverer libraryDiscoverer;

    public DefaultServiceDetailsAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryDiscoverer libraryDiscoverer) {
        this.gitInformationProvider = gitInformationProvider;
        this.buildProperties = providerBuildProperties.getIfAvailable();
        this.libraryDiscoverer = libraryDiscoverer;
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
        if (gitInformationProvider.getGitCommitInfo().isEmpty()) {
            return new GitDetails("", "", new GitDetails.CommitAuthor("", ""), "");
        }

        GitInfo gitCommitInfo = gitInformationProvider.getGitCommitInfo().get();
        GitInfo.CommitAuthor commitAuthor = gitCommitInfo.commitAuthor();
        return new GitDetails(
                emptyIfNull(gitCommitInfo.commitShaShort()),
                emptyIfNull(gitCommitInfo.branch()),
                new CommitAuthor(emptyIfNull(commitAuthor.name()), emptyIfNull(commitAuthor.email())),
                gitCommitInfo.commitTimestamp());
    }

    private SpringDetails getSpringDetails() {
        var springBootVersion = libraryDiscoverer.getLibraryVersion("spring-boot", "org.springframework.boot");
        var springVersion = libraryDiscoverer.getLibraryVersion("spring-core", "org.springframework");
        var springCloudVersion =
                libraryDiscoverer.getLibraryVersion("spring-cloud-commons", "org.springframework.cloud");
        return new SpringDetails(
                springBootVersion.orElse(""), springVersion.orElse(""), springCloudVersion.orElse(null));
    }

    private RuntimeDetails getRuntimeDetails() {
        String javaVersion = emptyIfNull(System.getProperty("java.version"));
        String jdkVendorFromVersion = System.getProperty("java.vendor.version");
        String jdkVendor =
                emptyIfNull(jdkVendorFromVersion != null ? jdkVendorFromVersion : System.getProperty("java.vendor"));
        String garbageCollector = getGarbageCollectorInfo();
        Optional<String> kotlinVersion = libraryDiscoverer.getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin");

        return new RuntimeDetails(javaVersion, jdkVendor, garbageCollector, kotlinVersion.orElse(null));
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
}
