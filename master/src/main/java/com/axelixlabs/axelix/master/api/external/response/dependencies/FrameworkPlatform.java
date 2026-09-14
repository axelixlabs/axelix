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

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

/**
 * The {@link Platform} that represents the Framework being used
 *
 * @param name                the name of the framework, e.g. {@code Spring Boot}
 * @param version             the exact version the instance runs
 * @param line                the release line the version belongs to
 * @param latestKnownLine     the most recent release line Axelix knows about
 * @param supportedTargetLine the release line a team on this framework is expected to move to
 *
 * @author Mikhail Polivakha
 */
public record FrameworkPlatform(
        PlatformName name,
        String version,
        PlatformReleaseLine line,
        PlatformReleaseLine latestKnownLine,
        PlatformReleaseLine supportedTargetLine) {}
