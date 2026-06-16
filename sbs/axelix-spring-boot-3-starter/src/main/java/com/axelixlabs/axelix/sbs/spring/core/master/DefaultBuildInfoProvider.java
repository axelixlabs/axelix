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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.info.BuildProperties;

import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Default {@link BuildInfoProvider} backed by the Spring Boot {@link BuildProperties}.
 *
 * <p>When no {@link BuildProperties} bean is available (i.e. the build plugin did not generate
 * {@code META-INF/build-info.properties}), every getter returns an empty string.
 *
 * @author Sergey Cherkasov
 */
public class DefaultBuildInfoProvider implements BuildInfoProvider {
    private final @Nullable BuildProperties buildProperties;

    public DefaultBuildInfoProvider(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public String getArtifact() {
        return buildProperties == null ? "" : emptyIfNull(buildProperties.getArtifact());
    }

    @Override
    public String getVersion() {
        return buildProperties == null ? "" : emptyIfNull(buildProperties.getVersion());
    }

    @Override
    public String getGroup() {
        return buildProperties == null ? "" : emptyIfNull(buildProperties.getGroup());
    }
}
