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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.convert.ScheduledTaskExecutionResultConverter;
import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile.Refresh;
import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile.Registration;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.scheduled.ScheduledTaskExecutionHistoryService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Job that performs periodical discovering and refresh of managed service instances in the registry.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Vyacheslav Yanin
 */
public class ShortPollingInstanceDiscoveryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShortPollingInstanceDiscoveryScheduler.class);

    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;
    private final DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService;
    private final TransactionTemplate transactionTemplate;
    private final ScheduledTaskExecutionResultConverter scheduledTaskExecutionResultConverter;
    private final ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService;
    private final DependencyAnalysisService dependencyAnalysisService;

    public ShortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry,
            DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService,
            TransactionTemplate transactionTemplate,
            ScheduledTaskExecutionResultConverter scheduledTaskExecutionResultConverter,
            ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService,
            DependencyAnalysisService dependencyAnalysisService) {

        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
        this.databaseHistoricalApplicationSnapshotService = databaseHistoricalApplicationSnapshotService;
        this.transactionTemplate = transactionTemplate;
        this.scheduledTaskExecutionResultConverter = scheduledTaskExecutionResultConverter;
        this.scheduledTaskExecutionHistoryService = scheduledTaskExecutionHistoryService;
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    @Scheduled(cron = "${axelix.master.discovery.auto.broadcast.schedule}")
    public void performDiscovery() {

        Set<DiscoveredInstanceProfile> discoveredInstances = instancesDiscoverer.discoverSafely();

        if (discoveredInstances.isEmpty()) {
            logger.error("""
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration.
                """, this.getClass().getSimpleName());
        }

        List<Registration> registrations = discoveredInstances.stream()
                .filter(Registration.class::isInstance)
                .map(Registration.class::cast)
                .toList();

        List<Refresh> refreshes = discoveredInstances.stream()
                .filter(Refresh.class::isInstance)
                .map(Refresh.class::cast)
                .toList();

        Set<BasicRegistrationMetadata> collectiveMetadata =
                registrations.stream().map(Registration::metadata).collect(Collectors.toSet());

        List<Set<ScheduledTaskExecutionResult>> scheduledTaskExecutionResultsOfInstances = Stream.concat(
                        registrations.stream().map(scheduledTaskExecutionResultConverter::convert),
                        refreshes.stream().map(scheduledTaskExecutionResultConverter::convert))
                .toList();

        List<InstanceId> discoveredNow = Stream.concat(
                        registrations.stream().map(Registration::instance),
                        refreshes.stream().map(Refresh::instance))
                .map(Instance::id)
                .toList();

        transactionTemplate.executeWithoutResult(_ -> {
            databaseHistoricalApplicationSnapshotService.reloadCurrentStateBulk(collectiveMetadata);
            scheduledTaskExecutionResultsOfInstances.forEach(scheduledTaskExecutionHistoryService::append);
            instanceRegistry.truncateAllExcept(discoveredNow);
            processRegistrations(registrations);
            processRefreshes(refreshes);
        });
    }

    private void processRefreshes(List<Refresh> refreshes) {
        for (Refresh refresh : refreshes) {
            databaseHistoricalApplicationSnapshotService.reloadTransactions(
                    refresh.instance().applicationId(), refresh.masterPing().getTransactions());

            instanceRegistry.refreshFromAutoDiscovery(
                    refresh.instance().id(),
                    Instance.InstanceStatus.fromHealthStatus(
                            refresh.masterPing().getHealthStatus()),
                    new MemoryUsage(refresh.masterPing().getMemoryDetails().getHeap()));
        }
    }

    private void processRegistrations(List<Registration> registrations) {
        for (Registration registration : registrations) {
            // The SBOM references the instance, so the instance has to be registered first
            instanceRegistry.register(registration.instance());

            // Just like the self-registration, the SBOM is only reported on the registration, never on a refresh
            if (registration.metadata().getSbom() != null) {
                dependencyAnalysisService.store(
                        registration.instance(), registration.metadata().getSbom());
            }
        }
    }
}
