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
package com.axelixlabs.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringFactoriesMergerTest {

    @Nested
    class WhenNoExistingFactories {

        @Test
        void addsBothAxelixEntries() {
            // given
            Map<String, String> empty = new LinkedHashMap<>();

            // when
            Map<String, String> merged = SpringFactoriesMerger.merge(empty);

            // then
            assertThat(merged)
                    .containsEntry(
                            SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY,
                            SpringFactoriesMerger.TEST_EXECUTION_LISTENER_VALUE)
                    .containsEntry(
                            SpringFactoriesMerger.APPLICATION_CONTEXT_INITIALIZER_KEY,
                            SpringFactoriesMerger.APPLICATION_CONTEXT_INITIALIZER_VALUE);
        }
    }

    @Nested
    class WhenConsumerAlreadyHasEntries {

        @Test
        void appendsToExistingKeyWithoutOverwriting() {
            // given
            Map<String, String> existing = new LinkedHashMap<>();
            existing.put(SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY, "com.example.OtherListener");

            // when
            Map<String, String> merged = SpringFactoriesMerger.merge(existing);

            // then
            assertThat(merged.get(SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY))
                    .isEqualTo("com.example.OtherListener," + SpringFactoriesMerger.TEST_EXECUTION_LISTENER_VALUE);
        }

        @Test
        void preservesUnrelatedKeys() {
            // given
            Map<String, String> existing = new LinkedHashMap<>();
            existing.put("com.example.SomeFactory", "com.example.SomeImpl");

            // when
            Map<String, String> merged = SpringFactoriesMerger.merge(existing);

            // then
            assertThat(merged).containsEntry("com.example.SomeFactory", "com.example.SomeImpl");
        }
    }

    @Nested
    class Idempotency {

        @Test
        void runningTwiceDoesNotDuplicateValues() {
            // given
            Map<String, String> factories = SpringFactoriesMerger.merge(new LinkedHashMap<>());

            // when
            Map<String, String> mergedAgain = SpringFactoriesMerger.merge(factories);

            // then
            assertThat(mergedAgain.get(SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY))
                    .isEqualTo(SpringFactoriesMerger.TEST_EXECUTION_LISTENER_VALUE);
            assertThat(mergedAgain.get(SpringFactoriesMerger.APPLICATION_CONTEXT_INITIALIZER_KEY))
                    .isEqualTo(SpringFactoriesMerger.APPLICATION_CONTEXT_INITIALIZER_VALUE);
        }
    }

    @Nested
    class ParsingAndRendering {

        @Test
        void parsesBackslashLineContinuations() {
            // given
            String content = "org.springframework.test.context.TestExecutionListener=\\\n"
                    + "digital.pragmatech.testing.SpringTestProfilerListener\n";

            // when
            Map<String, String> parsed = SpringFactoriesMerger.parse(content);

            // then
            assertThat(parsed)
                    .containsEntry(
                            SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY,
                            SpringFactoriesMerger.TEST_EXECUTION_LISTENER_VALUE);
        }

        @Test
        void skipsCommentsAndBlankLines() {
            // given
            String content = "# a comment\n\ncom.example.Key=com.example.Value\n";

            // when
            Map<String, String> parsed = SpringFactoriesMerger.parse(content);

            // then
            assertThat(parsed).hasSize(1).containsEntry("com.example.Key", "com.example.Value");
        }

        @Test
        void parseThenMergeThenRenderRoundTripsConsumerValue() {
            // given
            String content = SpringFactoriesMerger.TEST_EXECUTION_LISTENER_KEY + "=com.example.OtherListener\n";

            // when
            Map<String, String> merged = SpringFactoriesMerger.merge(SpringFactoriesMerger.parse(content));
            String rendered = SpringFactoriesMerger.render(merged);

            // then
            assertThat(rendered)
                    .contains("com.example.OtherListener," + SpringFactoriesMerger.TEST_EXECUTION_LISTENER_VALUE)
                    .contains(SpringFactoriesMerger.APPLICATION_CONTEXT_INITIALIZER_VALUE);
        }
    }
}
