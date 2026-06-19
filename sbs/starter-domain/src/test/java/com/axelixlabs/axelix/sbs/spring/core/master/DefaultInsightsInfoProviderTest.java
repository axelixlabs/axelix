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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.Insight;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.InsightFeature;
import com.axelixlabs.axelix.sbs.spring.core.gclog.JcmdExecutor;
import com.axelixlabs.axelix.sbs.spring.core.gclog.ProcessResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultInsightsInfoProvider}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
class DefaultInsightsInfoProviderTest {

    @Test
    void returnsDisabledInsights_whenOptionsAreEmptyAndOsivDisabled() {
        // given.
        var subject = new DefaultInsightsInfoProvider(List.of(), osivDisabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AppCDS", false);
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AotCache", false);
        assertFeatureEnabled(insight.getHotSpot().getGc(), "GC Logging", false);
        assertFeatureEnabled(insight.getHotSpot().getGc(), "GC Log file Specified", false);
        assertFeatureEnabled(insight.getHotSpot().getProjectLilliputh(), "Compressed Object Headers", false);
        assertFeatureEnabled(insight.getSpringFramework(), "OSIV", false);
    }

    @Test
    void returnsProjectLeydenInsightsEnabled_whenCorrespondingOptionsPresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                List.of("-XX:SharedArchiveFile=/path/to/archive.jsa", "-XX:AOTCache=/path/to/cache"),
                osivDisabled(),
                jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AppCDS", true);
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AotCache", true);
    }

    @Test
    void returnsAppCdsEnabled_whenSharedArchiveFilePresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                List.of("-Xmx256m", "-XX:SharedArchiveFile=/path/to/archive.jsa"), osivDisabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AppCDS", true);
    }

    @Test
    void returnsAppCdsDisabled_whenXshareOffPresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                List.of("-XX:SharedArchiveFile=/path/to/archive.jsa", "-Xshare:off"), osivDisabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AppCDS", false);
    }

    @Test
    void returnsAppCdsDisabled_whenXshareOffThenSharedArchiveFile() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                List.of("-Xshare:off", "-XX:SharedArchiveFile=/path/to/archive.jsa"), osivDisabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLeyden(), "AppCDS", false);
    }

    @Test
    void returnsGcLogFileSpecifiedEnabled_whenJcmdOutputContainsGcFileOutput() {
        // given.
        var subject =
                new DefaultInsightsInfoProvider(List.of(), osivDisabled(), jcmdOutput("#1: file=/tmp/gc.log gc=debug"));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getGc(), "GC Log file Specified", true);
    }

    @Test
    void returnsGcInsightsDisabled_whenJcmdFails() {
        // given.
        var subject = new DefaultInsightsInfoProvider(List.of(), osivDisabled(), failingJcmd());

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getGc(), "GC Logging", false);
        assertFeatureEnabled(insight.getHotSpot().getGc(), "GC Log file Specified", false);
    }

    @Test
    void returnsCompressedObjectHeadersEnabled_whenOptionPresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                List.of("-XX:+UseCompactObjectHeaders"), osivDisabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getHotSpot().getProjectLilliputh(), "Compressed Object Headers", true);
    }

    @Test
    void returnsOsivEnabled_whenOpenSessionInViewEnabled() {
        // given.
        var subject = new DefaultInsightsInfoProvider(List.of(), osivEnabled(), jcmdOutput(""));

        // when.
        Insight insight = subject.getInsight();

        // then.
        assertFeatureEnabled(insight.getSpringFramework(), "OSIV", true);
    }

    private static void assertFeatureEnabled(List<InsightFeature> features, String name, boolean enabled) {
        InsightFeature feature = findByName(features, name);

        assertThat(feature).isNotNull();
        assertThat(feature.isEnabled()).isEqualTo(enabled);
    }

    private static InsightFeature findByName(List<InsightFeature> features, String name) {
        return features.stream()
                .filter(f -> name.equals(f.getName()))
                .findFirst()
                .orElse(null);
    }

    private static JcmdExecutor jcmdOutput(String output) {
        return new TestJcmdExecutor(new ProcessResult(0, output), false);
    }

    private static JcmdExecutor failingJcmd() {
        return new TestJcmdExecutor(new ProcessResult(1, ""), true);
    }

    private static OpenSessionInViewStateProvider osivDisabled() {
        return () -> false;
    }

    private static OpenSessionInViewStateProvider osivEnabled() {
        return () -> true;
    }

    private static final class TestJcmdExecutor extends JcmdExecutor {

        private final ProcessResult result;
        private final boolean fail;

        private TestJcmdExecutor(ProcessResult result, boolean fail) {
            this.result = result;
            this.fail = fail;
        }

        @Override
        public ProcessResult execute(String... command) {
            if (fail) {
                throw new IllegalStateException("jcmd failed");
            }

            return result;
        }
    }
}
