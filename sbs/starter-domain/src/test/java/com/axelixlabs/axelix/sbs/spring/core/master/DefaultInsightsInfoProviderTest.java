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

import java.io.File;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatus;
import com.axelixlabs.axelix.common.api.registration.insights.InsightFeature;
import com.axelixlabs.axelix.common.api.registration.insights.Insights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.DefaultInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.VmOptionsAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static com.axelixlabs.axelix.common.domain.insights.FeatureId.AOT_CACHE;
import static com.axelixlabs.axelix.common.domain.insights.FeatureId.APP_CDS;
import static com.axelixlabs.axelix.common.domain.insights.FeatureId.COMPACT_OBJECT_HEADERS;
import static com.axelixlabs.axelix.common.domain.insights.FeatureId.GC_LOGGING_ENABLED;
import static com.axelixlabs.axelix.common.domain.insights.FeatureId.GC_LOG_FILE_SPECIFIED;
import static com.axelixlabs.axelix.common.domain.insights.FeatureId.OSIV;
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
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(), gcLogDisabled(), emptyVmOptions(), emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getProjectLeyden(), APP_CDS, false);
        assertFeatureEnabled(insights.getHotSpot().getProjectLeyden(), AOT_CACHE, false);
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOGGING_ENABLED, false);
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOG_FILE_SPECIFIED, false);
        assertFeatureEnabled(insights.getHotSpot().getProjectLilliputh(), COMPACT_OBJECT_HEADERS, false);
        assertFeatureEnabled(insights.getSpringFramework(), OSIV, false);
    }

    @Test
    void returnsProjectLeydenInsightsEnabled_whenCorrespondingOptionsPresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(),
                gcLogDisabled(),
                vmOptions("-XX:SharedArchiveFile=/path/to/archive.jsa", "-XX:AOTCache=/path/to/cache"),
                emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getProjectLeyden(), APP_CDS, true);
        assertFeatureEnabled(insights.getHotSpot().getProjectLeyden(), AOT_CACHE, true);
    }

    @Test
    void returnsAppCdsEnabled_whenSharedArchiveFilePresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(),
                gcLogDisabled(),
                vmOptions("-Xmx256m", "-XX:SharedArchiveFile=/path/to/archive.jsa"),
                emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getProjectLeyden(), APP_CDS, true);
    }

    @Test
    void returnsGcLoggingEnabled_whenGcLogServiceReportsEnabled() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(), gcLogEnabled(), emptyVmOptions(), emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOGGING_ENABLED, true);
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOG_FILE_SPECIFIED, false);
    }

    @Test
    void returnsGcLoggingDisabled_whenGcLogServiceReportsDisabled() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(), gcLogDisabled(), emptyVmOptions(), emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOGGING_ENABLED, false);
        assertFeatureEnabled(insights.getHotSpot().getGc(), GC_LOG_FILE_SPECIFIED, false);
    }

    @Test
    void returnsCompactObjectHeadersEnabled_whenOptionPresent() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivDisabled(),
                gcLogDisabled(),
                vmOptions("-XX:+UseCompactObjectHeaders"),
                emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getHotSpot().getProjectLilliputh(), COMPACT_OBJECT_HEADERS, true);
    }

    @Test
    void returnsOsivEnabled_whenOpenSessionInViewEnabled() {
        // given.
        var subject = new DefaultInsightsInfoProvider(
                osivEnabled(), gcLogDisabled(), emptyVmOptions(), emptyTransactionStatsCollector());

        // when.
        Insights insights = subject.getInsight();

        // then.
        assertFeatureEnabled(insights.getSpringFramework(), OSIV, true);
    }

    @Test
    void surfacesExternalCallsInPersistenceInsights() throws Exception {
        // given.
        DefaultTransactionStatsCollector collector = new DefaultTransactionStatsCollector();
        MethodClassKey key = new MethodClassKey(SampleService.class.getMethod("charge"), SampleService.class);
        TransactionExecutionProfile profile = new TransactionExecutionProfile(Instant.now());
        profile.recordExternalCall(
                new SimpleExternalCallRecord(TypeExternalCall.HTTP_CLIENT, "GET https://payments/charge", 15L));
        profile.complete();
        collector.recordTransaction(key, profile);

        var subject = new DefaultInsightsInfoProvider(osivDisabled(), gcLogDisabled(), emptyVmOptions(), collector);

        // when.
        Insights insights = subject.getInsight();

        // then.
        List<TransactionAggregatedProfile> transactions =
                insights.getPersistenceInsights().getTransactions();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getExternalCalls()).singleElement().satisfies(call -> {
            assertThat(call.getType()).isEqualTo(TypeExternalCall.HTTP_CLIENT);
            assertThat(call.getTarget()).isEqualTo("GET https://payments/charge");
            assertThat(call.getStats().getMinMs()).isEqualTo(15L);
            assertThat(call.getStats().getMaxMs()).isEqualTo(15L);
            assertThat(call.getStats().getAverageMs()).isEqualTo(15L);
        });
    }

    private static void assertFeatureEnabled(List<InsightFeature> features, FeatureId featureId, boolean enabled) {
        InsightFeature feature = findByFeatureId(features, featureId);

        assertThat(feature).isNotNull();
        assertThat(feature.isEnabled()).isEqualTo(enabled);
    }

    private static InsightFeature findByFeatureId(List<InsightFeature> features, FeatureId featureId) {
        return features.stream()
                .filter(f -> featureId.getId().equals(f.getFeatureId()))
                .findFirst()
                .orElse(null);
    }

    private static TransactionStatsCollector emptyTransactionStatsCollector() {
        return new DefaultTransactionStatsCollector();
    }

    private static VmOptionsAccessor emptyVmOptions() {
        return new VmOptionsAccessor(List.of());
    }

    private static VmOptionsAccessor vmOptions(String... options) {
        return new VmOptionsAccessor(List.of(options));
    }

    private static GcLogService gcLogDisabled() {
        return new TestGcLogService(new GcLogStatus(false, null, List.of("debug", "info")));
    }

    private static GcLogService gcLogEnabled() {
        return new TestGcLogService(new GcLogStatus(true, "debug", List.of("debug", "info")));
    }

    private static OpenSessionInViewStateProvider osivDisabled() {
        return () -> false;
    }

    private static OpenSessionInViewStateProvider osivEnabled() {
        return () -> true;
    }

    private static final class SampleService {
        public void charge() {}
    }

    private static final class TestGcLogService implements GcLogService {

        private final GcLogStatus status;

        private TestGcLogService(GcLogStatus status) {
            this.status = status;
        }

        @Override
        public GcLogStatus getStatus() {
            return status;
        }

        @Override
        public File getGcLogFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void enable(String level) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disable() {
            throw new UnsupportedOperationException();
        }
    }
}
