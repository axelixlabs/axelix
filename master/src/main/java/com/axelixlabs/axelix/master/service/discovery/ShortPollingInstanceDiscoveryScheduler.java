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

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.state.HistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Job that performs periodical discovering and refresh of managed service instances in the registry.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class ShortPollingInstanceDiscoveryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShortPollingInstanceDiscoveryScheduler.class);

    private static final PasswordlessUser TECH_USER = new PasswordlessUser("AXELIX.MASTER", Set.of());

    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;
    private final JwtEncoderService jwtEncoderService;
    private final SecurityContextExecutor securityContextExecutor;
    private final HistoricalApplicationSnapshotService historicalApplicationSnapshotService;
    private final TransactionTemplate transactionTemplate;

    public ShortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry,
            JwtEncoderService jwtEncoderService,
            SecurityContextExecutor securityContextExecutor,
            HistoricalApplicationSnapshotService historicalApplicationSnapshotService,
            TransactionTemplate transactionTemplate) {
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
        this.jwtEncoderService = jwtEncoderService;
        this.securityContextExecutor = securityContextExecutor;
        this.historicalApplicationSnapshotService = historicalApplicationSnapshotService;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(cron = "${axelix.master.discovery.auto.broadcast.schedule}")
    public void performDiscovery() {

        String token = jwtEncoderService.generateToken(TECH_USER, Duration.ofSeconds(300));

        Set<DiscoveredInstanceProfile> discoveredInstances = securityContextExecutor.callWithinSecurityContext(
                instancesDiscoverer::discoverSafely, new DefaultSecurityContext(TECH_USER, token));

        if (discoveredInstances.isEmpty()) {
            logger.error("""
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration.
                """, this.getClass().getSimpleName());
        }

        Set<BasicDiscoveryMetadata> collectiveMetadata = discoveredInstances.stream()
                .map(DiscoveredInstanceProfile::metadata)
                .collect(Collectors.toSet());

        Set<Instance> instances = discoveredInstances.stream()
                .map(DiscoveredInstanceProfile::instance)
                .collect(Collectors.toSet());

        transactionTemplate.executeWithoutResult(_ -> {
            instanceRegistry.reload(instances);
            historicalApplicationSnapshotService.reloadCurrentStateBulk(collectiveMetadata);
        });
    }
}
