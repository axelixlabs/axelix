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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The curated support policy of a single platform (e.g. {@code Spring Boot}): every release line Axelix knows about
 * and the maintenance window of each. This is what the version an instance runs is dated against.
 *
 * @param name  the name of the platform, e.g. {@code Spring Boot} or Quarkus
 * @param lines every release line of the platform Axelix knows about; never empty
 *
 * @author Mikhail Polivakha
 */
public record Platform(String name, List<PlatformReleaseLine> lines) {

    private static final Comparator<PlatformReleaseLine> BY_RELEASE_DATE =
            Comparator.comparing(PlatformReleaseLine::releasedAt);

    public Platform {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Platform '%s' declares no release lines, so no version could ever be dated".formatted(name));
        }

        if (lines.stream().map(PlatformReleaseLine::line).distinct().count() != lines.size()) {
            throw new IllegalArgumentException("Platform '%s' declares the same release line twice".formatted(name));
        }

        lines = List.copyOf(lines);
    }

    /**
     * The release line a concrete version belongs to, matched by the {@code major.minor} prefix: version
     * {@code 3.2.4} belongs to line {@code 3.2.x}.
     *
     * @param version the exact version an instance runs
     *
     * @return the line of the version, or empty when the version does not parse or the line is not curated
     */
    public Optional<PlatformReleaseLine> lineOf(String version) {
        String[] segments = version.split("\\.");

        if (segments.length < 2) {
            return Optional.empty();
        }

        String line = segments[0] + "." + segments[1] + ".x";

        return lines.stream().filter(candidate -> candidate.line().equals(line)).findFirst();
    }

    /**
     * @return the most recently released line of the platform
     */
    public PlatformReleaseLine latestKnownLine() {
        return lines.stream().max(BY_RELEASE_DATE).orElseThrow();
    }

    /**
     * The line a team on this platform is expected to move to: the most recently released line that still receives
     * OSS maintenance as of the given date, or the latest known line when every curated line has run out.
     *
     * @param today the date to judge against
     *
     * @return the line a team is expected to move to
     */
    public PlatformReleaseLine supportedTargetLine(LocalDate today) {
        return lines.stream()
                .filter(line -> line.ossSupportedAt(today))
                .max(BY_RELEASE_DATE)
                .orElseGet(this::latestKnownLine);
    }
}
