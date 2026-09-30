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
package com.axelixlabs.axelix.master.domain;

import org.springframework.data.relational.core.mapping.Column;

/**
 * The Java runtime version a managed service ran on, modeled as the JEP 322 (JDK 10+) version
 * coordinate {@code $FEATURE.$INTERIM.$UPDATE.$PATCH}.
 *
 * <ul>
 *   <li>{@code feature} - the feature-release counter, i.e. the number seen on the market
 *       (e.g. {@code 17}, {@code 21}, {@code 25}). Bumped every six months.</li>
 *   <li>{@code interim} - reserved for interim releases with no incompatible changes; in practice
 *       always {@code 0}, as it has never been used.</li>
 *   <li>{@code update} - the update release, bumped by the between-quarters security updates.</li>
 *   <li>{@code patch} - an emergency patch between update releases; almost always {@code 0}.</li>
 * </ul>
 *
 * Axelix only monitors Java 11+ applications, so the legacy {@code 1.x.0_u} version grammar is
 * intentionally not supported by {@link #parse(String)}.
 *
 * @param feature the feature release (e.g. {@code 21}).
 * @param interim the interim release (effectively always {@code 0}).
 * @param update  the update release.
 * @param patch   the emergency patch release (usually {@code 0}).
 *
 * @author Mikhail Polivakha
 */
public record JavaVersion(
        @Column("java_feature") int feature,
        @Column("java_interim") int interim,
        @Column("java_update") int update,
        @Column("java_patch") int patch) {

    /**
     * Parses a reported {@code java.version} string (e.g. {@code "21"}, {@code "21.0.2"}) into its
     * feature/interim/update/patch components by delegating to the JDK's own JEP 322 parser
     * ({@link Runtime.Version}). Trailing zero components are handled by the parser, so {@code "21"}
     * yields {@code (21, 0, 0, 0)}.
     *
     * @param reported the raw {@code java.version} value reported by the managed service.
     * @return the parsed {@link JavaVersion}.
     * @throws IllegalArgumentException if the string is not a valid JEP 322 version number.
     */
    public static JavaVersion parse(String reported) {
        Runtime.Version version = Runtime.Version.parse(reported);
        return new JavaVersion(version.feature(), version.interim(), version.update(), version.patch());
    }

    /**
     * Renders this version back to its canonical JEP 322 string, dropping trailing zero components the
     * same way Java itself prints versions. For example {@code (21, 0, 0, 0)} renders as {@code "21"} and
     * {@code (17, 0, 16, 0)} as {@code "17.0.16"}.
     *
     * @return the canonical, trailing-zero-trimmed version string.
     */
    public String render() {
        StringBuilder rendered = new StringBuilder().append(feature);
        if (patch != 0) {
            rendered.append('.')
                    .append(interim)
                    .append('.')
                    .append(update)
                    .append('.')
                    .append(patch);
        } else if (update != 0) {
            rendered.append('.').append(interim).append('.').append(update);
        } else if (interim != 0) {
            rendered.append('.').append(interim);
        }
        return rendered.toString();
    }
}
