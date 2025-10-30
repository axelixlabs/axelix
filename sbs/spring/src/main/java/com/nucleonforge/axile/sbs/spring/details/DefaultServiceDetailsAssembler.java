package com.nucleonforge.axile.sbs.spring.details;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.AxileDetails.BuildDetails;
import com.nucleonforge.axile.common.api.AxileDetails.GitDetails;
import com.nucleonforge.axile.common.api.AxileDetails.OsDetails;
import com.nucleonforge.axile.common.api.AxileDetails.RuntimeDetails;
import com.nucleonforge.axile.common.api.AxileDetails.SpringDetails;
import com.nucleonforge.axile.common.api.registration.GitInfo;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

import static com.nucleonforge.axile.sbs.spring.details.GarbageCollectorInfoAssembler.getGarbageCollectorInfo;
import static com.nucleonforge.axile.sbs.spring.utils.StringUtils.emptyIfNull;

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
    public AxileDetails assemble() {

        GitDetails git = getGitDetails();
        SpringDetails spring = getSpringDetails();
        RuntimeDetails runtime = getRuntimeDetails();
        BuildDetails build = getBuildDetails();
        OsDetails os = getOsDetails();

        // We intentionally set podName == null, this field will be initialized in Master
        return new AxileDetails(git, spring, runtime, build, os);
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
                new GitDetails.CommitAuthor(emptyIfNull(commitAuthor.name()), emptyIfNull(commitAuthor.email())),
                emptyIfNull(gitCommitInfo.commitTimestamp()));
    }

    private SpringDetails getSpringDetails() {
        Optional<String> springBootVersion =
                libraryDiscoverer.getLibraryVersion("spring-boot", "org.springframework.boot");
        Optional<String> springVersion = libraryDiscoverer.getLibraryVersion("spring-core", "org.springframework");
        Optional<String> springCloudVersion = libraryDiscoverer
                .getLibraryVersion("spring-cloud-context", "org.springframework.cloud")
                .or(() -> libraryDiscoverer.getLibraryVersion("spring-cloud-commons", "org.springframework.cloud"))
                .or(() -> libraryDiscoverer.getLibraryVersion("spring-cloud-starter", "org.springframework.cloud"));
        return new SpringDetails(springBootVersion.orElse(""), springVersion.orElse(""), springCloudVersion.orElse(""));
    }

    private RuntimeDetails getRuntimeDetails() {
        String javaVersion = emptyIfNull(System.getProperty("java.version"));
        String jdkVendor = emptyIfNull(System.getProperty("java.vendor.version"));
        String garbageCollector = getGarbageCollectorInfo();
        Optional<String> kotlinVersion = libraryDiscoverer.getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin");

        return new RuntimeDetails(javaVersion, jdkVendor, garbageCollector, kotlinVersion.orElse(""));
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
