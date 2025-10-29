package com.nucleonforge.axile.sbs.spring.details;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.Assert;

import com.nucleonforge.axile.common.api.details.AxileDetails;
import com.nucleonforge.axile.common.api.details.components.BuildDetails;
import com.nucleonforge.axile.common.api.details.components.GitDetails;
import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.common.api.details.components.RuntimeDetails;
import com.nucleonforge.axile.common.api.details.components.SpringDetails;
import com.nucleonforge.axile.common.api.registration.GitInfo;
import com.nucleonforge.axile.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

@SuppressWarnings("NullAway")
public class DefaultServiceDetailsAssembler implements ServiceDetailsAssembler {
    private final List<InfoContributor> infoContributors;
    private final CommitIdPluginGitInformationProvider gitInformationProvider;
    private final BuildProperties buildProperties;
    private final LibraryDiscoverer libraryDiscoverer;

    public DefaultServiceDetailsAssembler(
            List<InfoContributor> infoContributors,
            CommitIdPluginGitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryDiscoverer libraryDiscoverer) {
        Assert.notNull(infoContributors, "Info contributors must not be null");
        this.infoContributors = infoContributors;
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

        return new AxileDetails(git, spring, runtime, build, os);
    }

    private GitDetails getGitDetails() {
        if (gitInformationProvider.getGitCommitInfo().isEmpty()) {
            return null;
        }
        GitInfo gitCommitInfo = gitInformationProvider.getGitCommitInfo().get();
        GitInfo.CommitAuthor commitAuthor = gitCommitInfo.commitAuthor();
        return new GitDetails(
                gitCommitInfo.commitShaShort(),
                gitCommitInfo.branch(),
                new GitDetails.CommitAuthor(commitAuthor.name(), commitAuthor.email()),
                gitCommitInfo.commitTimestamp());
    }

    private SpringDetails getSpringDetails() {
        Optional<String> springBootVersion =
                libraryDiscoverer.getLibraryVersion("spring-boot", "org.springframework.boot");
        Optional<String> springVersion = libraryDiscoverer.getLibraryVersion("spring", "org.springframework");
        Optional<String> springCloudVersion =
                libraryDiscoverer.getLibraryVersion("spring-cloud", "org.springframework");
        return new SpringDetails(springBootVersion.orElse(""), springVersion.orElse(""), springCloudVersion.orElse(""));
    }

    private RuntimeDetails getRuntimeDetails() {
        String javaVersion = System.getProperty("java.version");
        String jdkVendor = System.getProperty("java.vendor.version");
        String garbageCollector = getGarbageCollectorInfo();
        Optional<String> springCloudVersion =
                libraryDiscoverer.getLibraryVersion("kotlin-stdlib", "org.jetbrains.kotlin");

        return new RuntimeDetails(javaVersion, jdkVendor, garbageCollector, springCloudVersion.orElse(""));
    }

    private String getGarbageCollectorInfo() {
        try {
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            if (!gcBeans.isEmpty()) {
                return gcBeans.stream().map(GarbageCollectorMXBean::getName).collect(Collectors.joining(", "));
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private BuildDetails getBuildDetails() {
        if (buildProperties == null) {
            return null;
        }

        return new BuildDetails(
                buildProperties.getArtifact(),
                buildProperties.getVersion(),
                buildProperties.getGroup(),
                buildProperties.getTime().toString());
    }

    private OsDetails getOsDetails() {
        return new OsDetails(
                System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }
}
