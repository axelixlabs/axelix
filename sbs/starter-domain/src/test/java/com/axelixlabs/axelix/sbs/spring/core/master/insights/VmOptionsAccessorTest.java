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
package com.axelixlabs.axelix.sbs.spring.core.master.insights;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VmOptionsAccessor}.
 *
 * @author Mikhail Polivakha
 */
class VmOptionsAccessorTest {

    @Nested
    class IsAdvancedFeatureEnabled {

        @Test
        void returnsFalse_whenOptionsAreEmpty() {
            // given.
            var subject = new VmOptionsAccessor(List.of());

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("UseCompactObjectHeaders"))
                    .isFalse();
        }

        @Test
        void returnsTrue_whenPlusPrefixedOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:+UseCompactObjectHeaders"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("UseCompactObjectHeaders"))
                    .isTrue();
        }

        @Test
        void returnsFalse_whenMinusPrefixedOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:-UseCompactObjectHeaders"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("UseCompactObjectHeaders"))
                    .isFalse();
        }

        @Test
        void returnsFalse_whenOnlyValueStyleOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:SharedArchiveFile=/path/to/archive.jsa"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("SharedArchiveFile")).isFalse();
        }

        @Test
        void returnsFalse_whenOptionDoesNotUseAdvancedPrefix() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-Xmx256m"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("mx256m")).isFalse();
        }

        @Test
        void usesFirstMatchingAdvancedOption() {
            // given.
            var subject =
                    new VmOptionsAccessor(List.of("-XX:-UseCompactObjectHeaders", "-XX:+UseCompactObjectHeaders"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureEnabled("UseCompactObjectHeaders"))
                    .isFalse();
        }
    }

    @Nested
    class IsAdvancedFeatureSpecified {

        @Test
        void returnsFalse_whenOptionsAreEmpty() {
            // given.
            var subject = new VmOptionsAccessor(List.of());

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureSpecified("SharedArchiveFile")).isFalse();
        }

        @Test
        void returnsTrue_whenSharedArchiveFileOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:SharedArchiveFile=/path/to/archive.jsa"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureSpecified("SharedArchiveFile")).isTrue();
        }

        @Test
        void returnsTrue_whenAotCacheOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:AOTCache=/path/to/cache"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureSpecified("AOTCache")).isTrue();
        }

        @Test
        void returnsFalse_whenOnlyStandardOptionPresent() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-Xmx256m", "-Xshare:off"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureSpecified("SharedArchiveFile")).isFalse();
        }

        @Test
        void returnsFalse_whenFlagNameIsOnlySubstring() {
            // given.
            var subject = new VmOptionsAccessor(List.of("-XX:NotSharedArchiveFile=/path"));

            // when.
            // then.
            assertThat(subject.isAdvancedFeatureSpecified("SharedArchiveFile")).isFalse();
        }
    }
}
