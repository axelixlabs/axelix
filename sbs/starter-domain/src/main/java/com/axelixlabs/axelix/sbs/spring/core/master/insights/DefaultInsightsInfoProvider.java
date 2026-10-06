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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.sbs.spring.core.contract.gclog.GcLogStatus;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ExecutionStats;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ExternalCallInsight;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HotSpotInsights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.InsightFeature;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.Insights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.LazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TransactionOrigin;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TransactionalKey;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogException;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.OpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.AggregatedExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.PerformanceStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionDefinitionAttributes;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStats;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory;

import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.AOT_CACHE_OPTION;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.SHARED_ARCHIVE_FILE;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.USE_COMPACT_OBJECT_HEADERS;

/**
 * Default {@link InsightsInfoProvider} based on the current service runtime configuration.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public class DefaultInsightsInfoProvider implements InsightsInfoProvider {

    private static final GcLogStatus DISABLED_GC_LOG_STATUS =
            new GcLogStatus().enabled(false).availableLevels(List.of());

    private final OpenSessionInViewStateProvider openSessionInViewStateProvider;

    @Nullable
    private final GcLogService gcLogService;

    private final VmOptionsAccessor vmOptionsAccessor;
    private final TransactionStatsCollector transactionStatsCollector;
    private final TransactionAttributesRegistry transactionAttributesRegistry;
    private final JpaEntitiesProfileProvider entitiesMapProvider;

    @Nullable
    private final ScheduledTaskExecutionHistory scheduledTaskExecutionHistory;

    /**
     * Creates a new DefaultInsightsInfoProvider.
     *
     * @param openSessionInViewStateProvider provider of the Spring Open Session in View state.
     * @param gcLogService                   GC log service, or {@code null} when the HotSpot DiagnosticCommandMBean
     *                                        is unavailable and GC log insights cannot be reported.
     * @param entitiesMapProvider            provider of the scanned JPA entities map.
     */
    public DefaultInsightsInfoProvider(
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            @Nullable GcLogService gcLogService,
            VmOptionsAccessor vmOptionsAccessor,
            TransactionStatsCollector transactionStatsCollector,
            TransactionAttributesRegistry transactionAttributesRegistry,
            JpaEntitiesProfileProvider entitiesMapProvider,
            @Nullable ScheduledTaskExecutionHistory scheduledTaskExecutionHistory) {
        this.openSessionInViewStateProvider = openSessionInViewStateProvider;
        this.gcLogService = gcLogService;
        this.vmOptionsAccessor = vmOptionsAccessor;
        this.transactionStatsCollector = transactionStatsCollector;
        this.transactionAttributesRegistry = transactionAttributesRegistry;
        this.entitiesMapProvider = entitiesMapProvider;
        this.scheduledTaskExecutionHistory = scheduledTaskExecutionHistory;
    }

    @Override
    public Insights getInsight() {
        GcLogStatus gcLogStatus = resolveGcLogStatus();

        return new Insights()
                .hotSpot(new HotSpotInsights()
                        .projectLeyden(List.of(getAppCdsFeature(), getAotCacheFeature()))
                        .gc(List.of(getGcLoggingFeature(gcLogStatus), getGcLogFileSpecifiedFeature()))
                        .projectLilliputh(List.of(getCompressedObjectHeadersFeature())))
                .springFramework(List.of(new InsightFeature()
                        .featureId(FeatureId.OSIV.getId())
                        .enabled(openSessionInViewStateProvider.isOpenSessionInViewEnabled())))
                .persistenceInsights(assemblePersistenceInsights())
                .scheduledTaskExecutions(collectScheduledTaskExecutions());
    }

    private List<ScheduledTaskExecution> collectScheduledTaskExecutions() {
        return scheduledTaskExecutionHistory != null ? scheduledTaskExecutionHistory.mark() : List.of();
    }

    private PersistenceInsights assemblePersistenceInsights() {
        Map<MethodClassKey, TransactionStats> stats = transactionStatsCollector.getCopyOfStats();

        List<TransactionAggregatedProfile> transactions = stats.entrySet().stream()
                .map(entry -> {
                    MethodClassKey key = entry.getKey();
                    TransactionStats transactionStats = entry.getValue();
                    PerformanceStats performanceStats = transactionStats.getPerformanceStats();
                    TransactionDefinitionAttributes attributes = transactionAttributesRegistry.get(key);

                    return new TransactionAggregatedProfile()
                            .transactionOrigin(TransactionOrigin.APPLICATION_DECLARATIVE)
                            .transactionalKey(new TransactionalKey()
                                    .className(key.getTargetClass().getName())
                                    .methodName(key.getMethod().getName()))
                            .transactionOverallStats(new ExecutionStats()
                                    .minMs(performanceStats.getMinMs())
                                    .maxMs(performanceStats.getMaxMs())
                                    .averageMs(performanceStats.getAvgMs()))
                            .lazyLoadingTargets(convertLazyLoadingTargets(transactionStats.getNPlusOneOccasions()))
                            .inMemoryPagination(new HashMap<>(transactionStats.getInMemoryPaginatedEntities()))
                            .externalCalls(convertExternalCalls(transactionStats.getExternalCalls()))
                            .propagation(attributes != null ? attributes.getPropagation() : null)
                            .isolation(attributes != null ? attributes.getIsolation() : null)
                            .readOnly(attributes != null && attributes.isReadOnly());
                })
                .collect(Collectors.toList());

        JpaEntities jpaEntities = entitiesMapProvider.getEntities();
        return new PersistenceInsights().transactions(transactions).entitiesMap(jpaEntities);
    }

    private static List<CountedLazyLoadingTarget> convertLazyLoadingTargets(
            Map<com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget, Integer>
                    nPlusOneOccasions) {
        return nPlusOneOccasions.entrySet().stream()
                .map(entry -> new CountedLazyLoadingTarget()
                        .target(new LazyLoadingTarget()
                                .ownerEntityClass(
                                        entry.getKey().ownerEntityClass().getName())
                                .associationPropertyName(entry.getKey().associationPropertyName()))
                        .count(entry.getValue()))
                .collect(Collectors.toList());
    }

    private static List<ExternalCallInsight> convertExternalCalls(List<AggregatedExternalCall> externalCalls) {
        return externalCalls.stream()
                .map(aggregatedCall -> {
                    PerformanceStats stats = aggregatedCall.getStats();
                    return new ExternalCallInsight()
                            .type(TypeExternalCall.valueOf(
                                    aggregatedCall.getType().name()))
                            .target(aggregatedCall.getTarget())
                            .stats(new ExecutionStats()
                                    .minMs(stats.getMinMs())
                                    .maxMs(stats.getMaxMs())
                                    .averageMs(stats.getAvgMs()));
                })
                .collect(Collectors.toList());
    }

    private InsightFeature getAppCdsFeature() {
        return new InsightFeature()
                .featureId(FeatureId.APP_CDS.getId())
                .enabled(vmOptionsAccessor.isAdvancedFeatureSpecified(SHARED_ARCHIVE_FILE));
    }

    private InsightFeature getAotCacheFeature() {
        return new InsightFeature()
                .featureId(FeatureId.AOT_CACHE.getId())
                .enabled(vmOptionsAccessor.isAdvancedFeatureSpecified(AOT_CACHE_OPTION));
    }

    private InsightFeature getCompressedObjectHeadersFeature() {
        return new InsightFeature()
                .featureId(FeatureId.COMPACT_OBJECT_HEADERS.getId())
                .enabled(vmOptionsAccessor.isAdvancedFeatureEnabled(USE_COMPACT_OBJECT_HEADERS));
    }

    private InsightFeature getGcLoggingFeature(GcLogStatus gcLogStatus) {
        return new InsightFeature()
                .featureId(FeatureId.GC_LOGGING_ENABLED.getId())
                .enabled(gcLogStatus.getEnabled());
    }

    private InsightFeature getGcLogFileSpecifiedFeature() {
        boolean fileSpecified = false;
        if (gcLogService != null) {
            try {
                fileSpecified = gcLogService.isGcLogFileSpecified();
            } catch (GcLogException ignored) {
                // Reporting GC-log insights must never break the rest of the insights payload.
            }
        }
        return new InsightFeature()
                .featureId(FeatureId.GC_LOG_FILE_SPECIFIED.getId())
                .enabled(fileSpecified);
    }

    private GcLogStatus resolveGcLogStatus() {
        if (gcLogService == null) {
            return DISABLED_GC_LOG_STATUS;
        }
        try {
            return gcLogService.getStatus();
        } catch (GcLogException e) {
            // Reporting GC-log insights must never break the rest of the insights payload.
            return DISABLED_GC_LOG_STATUS;
        }
    }
}
