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
package com.axelixlabs.axelix.common.utils;

import java.util.Comparator;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

/**
 * Immutable semantic version value object.
 * <p>
 * Parses a version string into its leading numeric {@code major.minor.patch} segments and an
 * optional trailing qualifier. At most three numeric segments are consumed; everything after
 * {@code patch} is the qualifier, which may be introduced by either a {@code .} or a {@code -}
 * separator. So postfixes such as {@code .RELEASE}, {@code .Final} or {@code -SNAPSHOT} are
 * recognised and stripped from the numeric accessors (e.g. {@code 3.0.15.RELEASE} yields major
 * {@code 3}, minor {@code 0}, patch {@code 15} and qualifier {@code RELEASE}).
 *
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class SemanticVersion implements Comparable<SemanticVersion> {

    private static final Comparator<SemanticVersion> ORDER = Comparator.comparingInt(SemanticVersion::major)
            .thenComparingInt(SemanticVersion::minor)
            .thenComparingInt(SemanticVersion::patch)
            // A qualifier (e.g. a pre-release like -SNAPSHOT) sorts below the same version without one.
            .thenComparing(version -> version.qualifier == null);

    private final int major;
    private final int minor;
    private final int patch;
    private final boolean hasMinor;
    private final boolean hasPatch;
    private final @Nullable String qualifier;

    private SemanticVersion(
            int major, int minor, int patch, boolean hasMinor, boolean hasPatch, @Nullable String qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.hasMinor = hasMinor;
        this.hasPatch = hasPatch;
        this.qualifier = qualifier;
    }

    /**
     * Parses the given version string.
     *
     * @param version the version string to parse
     * @return the parsed {@link SemanticVersion}
     * @throws IllegalArgumentException if the version is {@code null}, blank or does not start with
     *                                  a numeric major segment
     */
    public static SemanticVersion parse(String version) {
        return tryParse(version)
                .orElseThrow(() -> new IllegalArgumentException("Not a valid semantic version: " + version));
    }

    /**
     * Leniently parses the given version string.
     *
     * @param version the version string to parse, may be {@code null}
     * @return the parsed {@link SemanticVersion}, or {@link Optional#empty()} if the version is
     *         {@code null}, blank or does not start with a numeric major segment
     */
    public static Optional<SemanticVersion> tryParse(@Nullable String version) {
        if (version == null || version.isBlank()) {
            return Optional.empty();
        }

        String trimmed = version.trim();

        // Substring the leading major.minor.patch numeric run; everything after it is the qualifier.
        int[] numbers = {0, 0, 0};

        int[] scan;

        try {
            scan = scanNumericSegments(trimmed, numbers);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }

        int numericCount = scan[0];
        int qualifierStart = scan[1];

        if (numericCount == 0) {
            return Optional.empty();
        }

        return Optional.of(new SemanticVersion(
                numbers[0],
                numbers[1],
                numbers[2],
                numericCount >= 2,
                numericCount >= 3,
                qualifierFrom(trimmed, qualifierStart)));
    }

    /**
     * Reads the leading {@code major.minor.patch} numeric segments into {@code numbers}, splitting on
     * {@code .} like {@link String#indexOf(int)}.
     *
     * @return a two-element array of {@code [number of segments read, index where the qualifier starts]}
     */
    private static int[] scanNumericSegments(String version, int[] numbers) {
        int length = version.length();
        int count = 0;
        int pos = 0;
        while (count < numbers.length && pos < length) {
            int dot = version.indexOf('.', pos);
            int segmentEnd = dot == -1 ? length : dot;
            int digits = leadingDigitCount(version, pos, segmentEnd);
            if (digits == 0) {
                break;
            }
            numbers[count++] = Integer.parseInt(version.substring(pos, pos + digits));
            if (pos + digits < segmentEnd) {
                // A qualifier is glued to this segment without a separator, e.g. "19u" or "0-SNAPSHOT".
                pos += digits;
                break;
            }
            pos = dot == -1 ? length : dot + 1;
        }
        return new int[] {count, pos};
    }

    private static int leadingDigitCount(String version, int start, int end) {
        int pos = start;
        while (pos < end && Character.isDigit(version.charAt(pos))) {
            pos++;
        }
        return pos - start;
    }

    private static @Nullable String qualifierFrom(String version, int pos) {
        if (pos >= version.length()) {
            return null;
        }
        char separator = version.charAt(pos);
        int start = separator == '.' || separator == '-' ? pos + 1 : pos;
        return start < version.length() ? version.substring(start) : null;
    }

    /**
     * @return the major version segment, e.g. {@code 3}
     */
    public int major() {
        return major;
    }

    /**
     * @return the minor version segment, or {@code 0} when none was present
     */
    public int minor() {
        return minor;
    }

    /**
     * @return the {@code major.minor} segments, or just the major when no minor was present
     *         (e.g. {@code "3.0"}, but {@code "11"} for the input {@code "11"})
     */
    public String majorMinor() {
        return hasMinor ? major + "." + minor : Integer.toString(major);
    }

    /**
     * @return the numeric {@code major.minor.patch} version with the qualifier stripped; only the
     *         segments that were present are included (e.g. {@code "3.0.15"}, {@code "2.0"},
     *         {@code "11"})
     */
    public String versionNumber() {
        StringBuilder builder = new StringBuilder(Integer.toString(major));
        if (hasMinor) {
            builder.append('.').append(minor);
        }
        if (hasPatch) {
            builder.append('.').append(patch);
        }
        return builder.toString();
    }

    /**
     * @return the patch version segment, or {@code 0} when none was present
     */
    public int patch() {
        return patch;
    }

    /**
     * @return the trailing qualifier (e.g. {@code "RELEASE"}, {@code "Final"}, {@code "SNAPSHOT"}),
     *         or {@code null} when none was present
     */
    public @Nullable String qualifier() {
        return qualifier;
    }

    /**
     * Orders by numeric {@code major.minor.patch}; when those are equal, a version that carries a
     * qualifier (e.g. a pre-release like {@code -SNAPSHOT}) sorts below the same version without one.
     * The qualifier text itself is not ordered, so two qualified versions compare as equal — e.g.
     * {@code 1.0.0-SNAPSHOT} and {@code 1.0.0.RELEASE} are equal, but both are less than {@code 1.0.0}.
     *
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to,
     *         or greater than {@code other}
     */
    @Override
    public int compareTo(SemanticVersion other) {
        return ORDER.compare(this, other);
    }
}
