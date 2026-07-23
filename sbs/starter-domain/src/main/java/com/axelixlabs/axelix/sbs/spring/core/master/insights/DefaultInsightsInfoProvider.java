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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.api.LazyLoadingTarget;
import com.axelixlabs.axelix.common.api.gclog.GcLogStatus;
import com.axelixlabs.axelix.common.api.registration.insights.HotSpotInsights;
import com.axelixlabs.axelix.common.api.registration.insights.InsightFeature;
import com.axelixlabs.axelix.common.api.registration.insights.Insights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.ExecutionStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.ExternalCallInsight;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.OpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.AggregatedExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.PerformanceStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.AOT_CACHE_OPTION;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.SHARED_ARCHIVE_FILE;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.USE_COMPACT_OBJECT_HEADERS;

/**
 * Default {@link InsightsInfoProvider} based on the current service runtime configuration.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultInsightsInfoProvider implements InsightsInfoProvider {

    private final OpenSessionInViewStateProvider openSessionInViewStateProvider;

    private final GcLogService gcLogService;
    private final VmOptionsAccessor vmOptionsAccessor;
    private final TransactionStatsCollector transactionStatsCollector;

    /**
     * Creates a new DefaultInsightsInfoProvider.
     *
     * @param openSessionInViewStateProvider provider of the Spring Open Session in View state.
     */
    public DefaultInsightsInfoProvider(
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            GcLogService gcLogService,
            VmOptionsAccessor vmOptionsAccessor,
            TransactionStatsCollector transactionStatsCollector) {
        this.openSessionInViewStateProvider = openSessionInViewStateProvider;
        this.gcLogService = gcLogService;
        this.vmOptionsAccessor = vmOptionsAccessor;
        this.transactionStatsCollector = transactionStatsCollector;
    }

    @Override
    public Insights getInsight() {
        GcLogStatus gcLogStatus = gcLogService.getStatus();

        return new Insights(
                new HotSpotInsights(
                        List.of(getAppCdsFeature(), getAotCacheFeature()),
                        List.of(getGcLoggingFeature(gcLogStatus), getGcLogFileSpecifiedFeature()),
                        List.of(getCompressedObjectHeadersFeature())),
                List.of(new InsightFeature(
                        FeatureId.OSIV.getId(), openSessionInViewStateProvider.isOpenSessionInViewEnabled())),
                assemblePersistenceInsights());
    }

    private PersistenceInsights assemblePersistenceInsights() {
        Map<MethodClassKey, TransactionStats> stats = transactionStatsCollector.getCopyOfStats();

        List<TransactionAggregatedProfile> transactions = stats.entrySet().stream()
                .map(entry -> {
                    MethodClassKey key = entry.getKey();
                    TransactionStats transactionStats = entry.getValue();
                    PerformanceStats performanceStats = transactionStats.getPerformanceStats();

                    return new TransactionAggregatedProfile(
                            TransactionOrigin.APPLICATION_DECLARATIVE,
                            new TransactionalKey(
                                    key.getTargetClass().getName(),
                                    key.getMethod().getName()),
                            new ExecutionStats(
                                    performanceStats.getMinMs(),
                                    performanceStats.getMaxMs(),
                                    performanceStats.getAvgMs()),
                            convertLazyLoadingTargets(transactionStats.getNPlusOneOccasions()),
                            new HashMap<>(transactionStats.getInMemoryPaginatedEntities()),
                            convertExternalCalls(transactionStats.getExternalCalls()));
                })
                .collect(Collectors.toList());

        return new PersistenceInsights(transactions);
    }

    private static List<CountedLazyLoadingTarget> convertLazyLoadingTargets(
            Map<com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget, Integer>
                    nPlusOneOccasions) {
        return nPlusOneOccasions.entrySet().stream()
                .map(entry -> new CountedLazyLoadingTarget(
                        new LazyLoadingTarget(
                                entry.getKey().ownerEntityClass().getName(),
                                entry.getKey().associationPropertyName()),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    private static List<ExternalCallInsight> convertExternalCalls(List<AggregatedExternalCall> externalCalls) {
        return externalCalls.stream()
                .map(aggregatedCall -> {
                    PerformanceStats stats = aggregatedCall.getStats();
                    return new ExternalCallInsight(
                            aggregatedCall.getType(),
                            aggregatedCall.getTarget(),
                            new ExecutionStats(stats.getMinMs(), stats.getMaxMs(), stats.getAvgMs()));
                })
                .collect(Collectors.toList());
    }

    private InsightFeature getAppCdsFeature() {
        return new InsightFeature(
                FeatureId.APP_CDS.getId(), vmOptionsAccessor.isAdvancedFeatureSpecified(SHARED_ARCHIVE_FILE));
    }

    private InsightFeature getAotCacheFeature() {
        return new InsightFeature(
                FeatureId.AOT_CACHE.getId(), vmOptionsAccessor.isAdvancedFeatureSpecified(AOT_CACHE_OPTION));
    }

    private InsightFeature getCompressedObjectHeadersFeature() {
        return new InsightFeature(
                FeatureId.COMPACT_OBJECT_HEADERS.getId(),
                vmOptionsAccessor.isAdvancedFeatureEnabled(USE_COMPACT_OBJECT_HEADERS));
    }

    private InsightFeature getGcLoggingFeature(GcLogStatus gcLogStatus) {
        return new InsightFeature(FeatureId.GC_LOGGING_ENABLED.getId(), gcLogStatus.isEnabled());
    }

    private InsightFeature getGcLogFileSpecifiedFeature() {
        return new InsightFeature(FeatureId.GC_LOG_FILE_SPECIFIED.getId(), gcLogService.isGcLogFileSpecified());
    }
}
