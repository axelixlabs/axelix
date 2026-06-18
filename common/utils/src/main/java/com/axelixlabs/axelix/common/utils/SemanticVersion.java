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

import org.jspecify.annotations.NonNull;
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
    private final @Nullable String qualifier;

    SemanticVersion(int major, int minor, int patch, @Nullable String qualifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
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

        NumericParsingState result = parseNumeric(trimmed);
        if (result == null) {
            return Optional.empty();
        }

        String qualifier;

        try {
            qualifier = parseQualifier(trimmed, result.getPos());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        return Optional.of(new SemanticVersion(result.getMajor(), result.getMinor(), result.getPatch(), qualifier));
    }

    /**
     * Parses numeric part from given version string
     * @param version version string
     *
     * @return {@link NumericParsingState} or {@code null} when invalid version format
     */
    private static @Nullable NumericParsingState parseNumeric(String version) {
        int majorEnd = version.indexOf('.');
        int minorEnd = majorEnd == -1 ? -1 : version.indexOf('.', majorEnd + 1);
        int patchEnd = minorEnd == -1 ? -1 : getLastDigitIdx(version, minorEnd + 1, version.length());

        if (majorEnd == -1 || minorEnd == -1 || patchEnd == -1) {
            return null;
        }

        Integer major = parseNumber(version, 0, majorEnd);
        Integer minor = parseNumber(version, majorEnd + 1, minorEnd);
        Integer patch = parseNumber(version, minorEnd + 1, patchEnd);

        if (major == null || minor == null || patch == null) {
            return null;
        }

        return new NumericParsingState(major, minor, patch, patchEnd);
    }

    /**
     * Parses qualifier from given version string
     * @param version version string
     * @param pos position of last patch digit
     *
     * @return qualifier string
     *
     * @throws IllegalArgumentException when version haven't separator between patch and qualifier
     * @throws IllegalArgumentException when version haven't content after separator
     */
    private static @Nullable String parseQualifier(String version, int pos) {
        String qualifier = null;

        if (pos < version.length()) {
            if (pos + 2 > version.length()) {
                throw new IllegalArgumentException("qualifier should have content after separator");
            }

            char separator = version.charAt(pos);

            if (separator == '#' || Character.isLetter(separator)) {
                throw new IllegalArgumentException("version must have separator between patch and qualifier");
            }

            qualifier = version.substring(pos + 1);
        }

        return qualifier;
    }

    /**
     * Parses number from version string
     *
     * @param version version string
     * @param start idx of first digit
     * @param end idx of last digit
     *
     * @return number from version string or {@code null} when invalid number
     */
    private static @Nullable Integer parseNumber(String version, int start, int end) {
        String substr = version.substring(start, end);

        try {
            return Integer.parseInt(substr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @param version string
     * @param start first char idx
     * @param end last char idx
     * @return idx of last digit in selected range in version string
     */
    private static int getLastDigitIdx(@NonNull String version, int start, int end) {
        int result = start;

        while (result < end) {
            char c = version.charAt(result);

            if (!Character.isDigit(c)) {
                break;
            }

            result++;
        }
        return result;
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
        return major + "." + minor;
    }

    /**
     * @return the numeric {@code major.minor.patch} version with the qualifier stripped; only the
     *         segments that were present are included (e.g. {@code "3.0.15"}, {@code "2.0"},
     *         {@code "11"})
     */
    public String versionNumber() {
        return major + "." + minor + "." + patch;
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
    public int compareTo(@NonNull SemanticVersion other) {
        return ORDER.compare(this, other);
    }

    /**
     * State of numeric version parsing process
     */
    private static class NumericParsingState {
        private final int major;
        private final int minor;
        private final int patch;
        private final int pos;

        NumericParsingState(int major, int minor, int patch, int pos) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.pos = pos;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getPatch() {
            return patch;
        }

        public int getPos() {
            return pos;
        }
    }
}
