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
 * The platform that Axelix Master is aware about.
 * <p>
 * Platform is considered to be a fundamental part of the application. It may be, for example:
 *
 * @param name  the name of the platform, e.g. {@code Spring Boot} or Quarkus
 * @param lines every release line of the platform Axelix knows about; never empty
 *
 * @author Mikhail Polivakha
 */
public record Platform(PlatformName name, List<PlatformReleaseLine> lines) {

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
     * Extracts the release line from the given version. By "release line" we call the certain bundle of versions that
     * are considered to belong to same "line". In case of OpenJDK release like is java feature version, in case of
     * spring boot/framework it is the generation version e.g. Spring Boot 2.7.x or Spring Framework 6.1.x.
     *
     * @param version the exact version an instance runs
     *
     * @return the line of the version, or empty when the version does not parse or the line is not curated
     */
    public Optional<PlatformReleaseLine> lineOf(String version) {
        return switch (name.versionFormat()) {
            case SPRING -> lineOfSpring(version);
            case OPENJDK -> lineOfOpenJDKDistro(version);
        };
    }

    private Optional<PlatformReleaseLine> lineOfSpring(String version) {
        String[] segments = version.split("\\.");
        if (segments.length < 2) {
            return Optional.empty();
        }
        return findLine(segments[0] + "." + segments[1] + ".x");
    }

    private Optional<PlatformReleaseLine> lineOfOpenJDKDistro(String version) {
        try {
            return findLine(Integer.toString(Runtime.Version.parse(version).feature()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<PlatformReleaseLine> findLine(String line) {
        return lines.stream().filter(candidate -> candidate.line().equals(line)).findFirst();
    }

    /**
     * @return the most recently released line of the platform
     */
    public PlatformReleaseLine latestKnownReleaseLine() {
        return lines.stream().max(BY_RELEASE_DATE).orElseThrow();
    }

    /**
     * Returns the minimal known release line, that is still OSS supported for the given Platform.
     *
     * @return the line a team is expected to move to
     */
    public PlatformReleaseLine minimalOssSupportedReleaseLine() {
        return lines.stream()
                .filter(line -> line.ossSupportedAt(LocalDate.now()))
                .min(BY_RELEASE_DATE)
                .orElseGet(this::latestKnownReleaseLine);
    }
}
