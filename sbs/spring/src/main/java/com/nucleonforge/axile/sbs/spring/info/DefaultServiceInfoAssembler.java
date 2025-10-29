package com.nucleonforge.axile.sbs.spring.info;

import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.util.Assert;

import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

public class DefaultServiceInfoAssembler implements ServiceInfoAssembler {
    private final List<InfoContributor> infoContributors;
    private final GitInformation gitInformation;
    private final KotlinVersionProvider kotlinVersionProvider;
    private final LibraryDiscoverer libraryDiscoverer;

    public DefaultServiceInfoAssembler(
            List<InfoContributor> infoContributors,
            GitInformation gitInformation,
            KotlinVersionProvider kotlinVersionProvider,
            LibraryDiscoverer libraryDiscoverer) {
        Assert.notNull(infoContributors, "Info contributors must not be null");
        this.infoContributors = infoContributors;
        this.gitInformation = gitInformation;
        this.kotlinVersionProvider = kotlinVersionProvider;
        this.libraryDiscoverer = libraryDiscoverer;
    }

    @Override
    public Map<String, Object> assemble() {
        Info.Builder builder = new Info.Builder();

        for (InfoContributor contributor : this.infoContributors) {
            contributor.contribute(builder);
        }

        gitInformation
                .getGitCommitInfo()
                .ifPresent(gitInfo -> builder.withDetail(
                        "git",
                        Map.of(
                                "commitId", gitInfo.commitShaShort(),
                                "branch", gitInfo.branch(),
                                "commitTime", gitInfo.commitTimestamp(),
                                "commitAuthor", gitInfo.commitAuthor())));

        String kotlinVersion = kotlinVersionProvider.getVersion();
        builder.withDetail("kotlin", Map.of("versionKotlon", kotlinVersion));

        var cloudVersion = libraryDiscoverer
                .getLibraryVersion("spring-cloud-starter-config", "org.springframework.cloud")
                .orElse("");
        builder.withDetail("springCloudVersion", Map.of("version", cloudVersion));

        return builder.build().getDetails();
    }
}
