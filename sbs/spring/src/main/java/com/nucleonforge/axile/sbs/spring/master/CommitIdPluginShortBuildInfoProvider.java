package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import org.springframework.boot.info.GitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.nucleonforge.axile.common.api.registration.ShortBuildInfo;

@Order(Ordered.LOWEST_PRECEDENCE)
public class CommitIdPluginShortBuildInfoProvider implements ShortBuildInfoProvider {

    private static final String BUILD_TIME = "build.time";
    private static final String BUILD_VERSION = "build.version";

    private final GitProperties gitProperties;

    public CommitIdPluginShortBuildInfoProvider(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Override
    public Optional<ShortBuildInfo> getShortBuildInfo() {
        return Optional.of(new ShortBuildInfo(gitProperties.get(BUILD_TIME), gitProperties.get(BUILD_VERSION)));
    }
}
