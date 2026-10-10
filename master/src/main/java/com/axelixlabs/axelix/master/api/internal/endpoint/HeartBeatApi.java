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
package com.axelixlabs.axelix.master.api.internal.endpoint;

import java.time.Instant;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.master.api.internal.ApiPaths;
import com.axelixlabs.axelix.master.api.internal.InternalApiRestController;
import com.axelixlabs.axelix.master.contract.heartbeat.HeartBeatMetadata;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.service.InstanceFactory;
import com.axelixlabs.axelix.master.service.convert.ScheduledTaskExecutionResultConverter;
import com.axelixlabs.axelix.master.service.discovery.CompatibilityDetectionStrategy;
import com.axelixlabs.axelix.master.service.scheduled.ScheduledTaskExecutionHistoryService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * The API used for service self-registration.
 *
 * @author Sergey Cherkasov
 * @author Vyacheslav Yanin
 */
@InternalApiRestController
@ConditionalOnProperty(
        prefix = "axelix.master.discovery.self-registration",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class HeartBeatApi {

    private static final Logger log = LoggerFactory.getLogger(HeartBeatApi.class);

    private final InstanceRegistry instanceRegistry;
    private final InstanceFactory instanceFactory;
    private final DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService;
    private final TransactionTemplate transactionTemplate;
    private final CompatibilityDetectionStrategy compatibilityDetectionStrategy;
    private final ScheduledTaskExecutionResultConverter scheduledTaskExecutionResultConverter;
    private final ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService;

    public HeartBeatApi(
            InstanceRegistry instanceRegistry,
            InstanceFactory instanceFactory,
            DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService,
            TransactionTemplate transactionTemplate,
            CompatibilityDetectionStrategy compatibilityDetectionStrategy,
            ScheduledTaskExecutionResultConverter scheduledTaskExecutionResultConverter,
            ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService) {
        this.instanceRegistry = instanceRegistry;
        this.instanceFactory = instanceFactory;
        this.databaseHistoricalApplicationSnapshotService = databaseHistoricalApplicationSnapshotService;
        this.transactionTemplate = transactionTemplate;
        this.compatibilityDetectionStrategy = compatibilityDetectionStrategy;
        this.scheduledTaskExecutionResultConverter = scheduledTaskExecutionResultConverter;
        this.scheduledTaskExecutionHistoryService = scheduledTaskExecutionHistoryService;
    }

    @PostMapping(path = ApiPaths.HeartBeatApi.SERVICE_REGISTER)
    public ResponseEntity<Void> registryServiceInstance(@RequestBody HeartBeatMetadata request) {

        BasicRegistrationMetadata metadata = request.getBasicRegistrationMetadata();
        String starterVersion = metadata.getVersion();

        if (!compatibilityDetectionStrategy.isCompatible(starterVersion)) {
            log.warn(
                    "Rejecting self-registration request from '{}': its Axelix starter version '{}' is not supported by this Axelix Master",
                    request.getInstanceName(),
                    starterVersion);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        try {
            Instance instance = instanceFactory.createInstance(
                    request.getInstanceId(),
                    request.getInstanceName(),
                    request.getDeploymentAt(),
                    Instant.now(),
                    request.getInstanceActuatorUrl(),
                    metadata);

            Set<ScheduledTaskExecutionResult> scheduledTaskExecutionResults =
                    scheduledTaskExecutionResultConverter.convert(request);

            transactionTemplate.executeWithoutResult(_ -> {
                instanceRegistry.reload(instance);
                databaseHistoricalApplicationSnapshotService.reloadCurrentState(metadata);
                scheduledTaskExecutionHistoryService.append(scheduledTaskExecutionResults);
            });

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException iae) {
            log.warn("Unable to process self-registration request from '{}'", request.getInstanceName(), iae);
            return ResponseEntity.badRequest().build();
        }
    }
}
