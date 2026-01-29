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
package com.axelixlabs.axelix.sbs.spring.master;

import java.util.Optional;

import org.springframework.boot.info.GitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;

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
