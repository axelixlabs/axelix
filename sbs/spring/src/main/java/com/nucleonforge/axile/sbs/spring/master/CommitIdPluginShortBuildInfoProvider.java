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
