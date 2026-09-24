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
package com.axelixlabs.axelix.master.api.external.response.dependencies;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

/**
 * The {@link Platform} that represents the Framework being used in the particular app.
 *
 * @param name                the name of the framework, e.g. {@code Spring Boot}.
 * @param version             the exact version of the framework the app runs.
 * @param line                the release line the version belongs to. Might be null if Axelix Master does
 *                            not know this release line of the framework.
 * @param latestKnownLine     the most recent framework release line Axelix knows about.
 * @param oldestSupportedLine the oldest framework release line that is still supported.
 *
 * @author Mikhail Polivakha
 */
public record AppFrameworkInfo(
        String name,
        String version,
        @Nullable PlatformReleaseLine line,
        PlatformReleaseLine latestKnownLine,
        PlatformReleaseLine oldestSupportedLine) {}
