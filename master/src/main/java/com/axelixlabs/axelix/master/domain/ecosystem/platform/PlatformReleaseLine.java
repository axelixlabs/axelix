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
package com.axelixlabs.axelix.master.domain.ecosystem.platform;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * One release line of a platform that is subject for any kind of OSS or Enterprise support. Now, what do I mean.
 * The "subject to support" release line may be:
 *
 * <ul>
 *     <li>The minor release line {@code 3.2.x} of Spring Boot, since Spring Boot provides support for minor releases</li>
 *     <li>The {@code 21} release of OpenJDK by some vendor (e.g. Oracle). Vendors of OpenJDK provide support per each Java version</li>
 * </ul>
 *
 * I hope now it is more clear. This domain object also includes the maintenance window,
 * as published by the upstream support policy of the corresponding platform.
 *
 * @param line                    the release line that is subject to OSS or enterprise support. Maybe written in an
 *                                arbitrary ways.
 * @param releasedAt              the date the line was first released.
 * @param ossSupportEndsAt        the date OSS maintenance (CVE patches etc.) of the line ended or will end if not already.
 * @param commercialSupportEndsAt the date commercial support of the line ends, or null when the upstream offers no
 *                                commercial support at all.
 *
 * @author Mikhail Polivakha
 */
public record PlatformReleaseLine(
        String line,
        LocalDate releasedAt,
        LocalDate ossSupportEndsAt,
        @Nullable LocalDate commercialSupportEndsAt) {

    /**
     * @param today the date to judge against
     *
     * @return whether the line still receives OSS maintenance as of the given date
     */
    public boolean ossSupportedAt(LocalDate today) {
        return !today.isAfter(ossSupportEndsAt);
    }
}
