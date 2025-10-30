package com.nucleonforge.axile.sbs.spring.details;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.api.registration.GitInfo;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

import static com.nucleonforge.axile.sbs.spring.details.GarbageCollectorInfoAssembler.getGarbageCollectorInfo;
import static com.nucleonforge.axile.sbs.spring.utils.StringUtils.checkNull;

public class DefaultServiceDetailsAssembler implements ServiceDetailsAssembler {
    private final GitInformationProvider gitInformationProvider;
    private final BuildProperties buildProperties;
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

        AxileDetails.GitDetails git = getGitDetails();
        AxileDetails.SpringDetails spring = getSpringDetails();
        AxileDetails.RuntimeDetails runtime = getRuntimeDetails();
        AxileDetails.BuildDetails build = getBuildDetails();
        AxileDetails.OsDetails os = getOsDetails();

        // We intentionally set podName == null, this field will be initialized in Master
        return new AxileDetails(null, git, spring, runtime, build, os);
    }

    private AxileDetails.GitDetails getGitDetails() {
        if (gitInformationProvider.getGitCommitInfo().isEmpty()) {
            return null;
        }

        GitInfo gitCommitInfo = gitInformationProvider.getGitCommitInfo().get();
        GitInfo.CommitAuthor commitAuthor = gitCommitInfo.commitAuthor();
        return new AxileDetails.GitDetails(
                checkNull(gitCommitInfo.commitShaShort()),
                checkNull(gitCommitInfo.branch()),
                new AxileDetails.GitDetails.CommitAuthor(
                        checkNull(commitAuthor.name()), checkNull(commitAuthor.email())),
                checkNull(gitCommitInfo.commitTimestamp()));
    }

    private AxileDetails.SpringDetails getSpringDetails() {
        Optional<String> springBootVersion =
                libraryDiscoverer.getLibraryVersion("spring-boot", "org.springframework.boot");
        Optional<String> springVersion = libraryDiscoverer.getLibraryVersion("spring-core", "org.springframework");
        Optional<String> springCloudVersion = libraryDiscoverer
                .getLibraryVersion("spring-cloud-context", "org.springframework.cloud")
                .or(() -> libraryDiscoverer.getLibraryVersion("spring-cloud-commons", "org.springframework.cloud"))
                .or(() -> libraryDiscoverer.getLibraryVersion("spring-cloud-starter", "org.springframework.cloud"));
        return new AxileDetails.SpringDetails(
                springBootVersion.orElse(""), springVersion.orElse(""), springCloudVersion.orElse(""));
    }

    private AxileDetails.RuntimeDetails getRuntimeDetails() {
        String javaVersion = checkNull(System.getProperty("java.version"));
        String jdkVendor = checkNull(System.getProperty("java.vendor.version"));
        String garbageCollector = getGarbageCollectorInfo();
        Optional<String> kotlinVersion = libraryDiscoverer.getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin");

        return new AxileDetails.RuntimeDetails(javaVersion, jdkVendor, garbageCollector, kotlinVersion.orElse(""));
    }

    private AxileDetails.BuildDetails getBuildDetails() {
        if (buildProperties == null) {
            return null;
        }

        return new AxileDetails.BuildDetails(
                checkNull(buildProperties.getArtifact()),
                checkNull(buildProperties.getVersion()),
                checkNull(buildProperties.getGroup()),
                checkNull(buildProperties.getTime().toString()));
    }

    private AxileDetails.OsDetails getOsDetails() {
        return new AxileDetails.OsDetails(
                checkNull(System.getProperty("os.name")),
                checkNull(System.getProperty("os.version")),
                checkNull(System.getProperty("os.arch")));
    }
}
