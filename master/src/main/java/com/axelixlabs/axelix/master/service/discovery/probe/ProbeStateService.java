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
package com.axelixlabs.axelix.master.service.discovery.probe;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.ProbeState;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;
import com.axelixlabs.axelix.master.repository.ProbeStateRepository;
import com.axelixlabs.axelix.master.service.discovery.DiscoveryLock;
import com.axelixlabs.axelix.master.service.discovery.probe.backoff.ProbeBackoffProperties;

/**
 * Keeps the {@link ProbeState} of the auto-discovered instances. The claims of the Master replicas are serialized
 * by the {@link DiscoveryLock}, so that the same instance is never probed by two replicas at the same time. The
 * probes themselves run without the lock, and a result is recorded only if the instance has not been re-claimed
 * in the meantime.
 *
 * @author Marsel Semenov
 */
@Service
public class ProbeStateService {

    private static final String RECORD_RESULT = """
            UPDATE probe_state
            SET consecutive_failures = :consecutiveFailures,
                last_verdict         = :lastVerdict,
                last_status          = :lastStatus,
                next_attempt_at      = :nextAttemptAt,
                last_success_at      = :lastSuccessAt,
                version              = version + 1
            WHERE service_id = :serviceId AND host = :host AND port = :port AND version = :version
            """;

    private final ProbeStateRepository probeStateRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final NamedParameterJdbcOperations jdbcOperations;
    private final JdbcConverter jdbcConverter;
    private final DiscoveryLock discoveryLock;
    private final ProbeBackoffProperties properties;

    public ProbeStateService(
            ProbeStateRepository probeStateRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            NamedParameterJdbcOperations jdbcOperations,
            JdbcConverter jdbcConverter,
            DiscoveryLock discoveryLock,
            ProbeBackoffProperties properties) {
        this.probeStateRepository = probeStateRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.jdbcOperations = jdbcOperations;
        this.jdbcConverter = jdbcConverter;
        this.discoveryLock = discoveryLock;
        this.properties = properties;
    }

    /**
     * Registers the instances seen for the first time, and claims the instances that are due for probing by moving
     * their next attempt past the lease. Existing states are left untouched by the registration, so that the
     * backoff and the failure counter survive.
     * <p>
     * The states that are due, but no longer visible, belong to instances that have disappeared, and are removed.
     * Every state becomes due at least once per {@link ProbeBackoffProperties#permanent()}, so the states of the
     * disappeared instances do not outlive it.
     *
     * @param visible all the instances currently visible to the discovery
     * @return only the states claimed by this call
     */
    @Transactional
    public Map<InstanceKey, ProbeState> registerAndClaimDue(Collection<InstanceKey> visible, Instant now) {
        discoveryLock.acquire();

        Set<InstanceKey> known = probeStateRepository.findAllById(visible).stream()
                .map(ProbeState::key)
                .collect(Collectors.toSet());

        List<ProbeState> toInsert = visible.stream()
                .filter(key -> !known.contains(key))
                .map(key -> ProbeState.initial(key, now))
                .toList();
        jdbcAggregateTemplate.insertAll(toInsert);

        Set<InstanceKey> visibleKeys = Set.copyOf(visible);
        Map<Boolean, List<ProbeState>> dueByVisibility =
                probeStateRepository.findAllByNextAttemptAtLessThanEqual(now).stream()
                        .collect(Collectors.partitioningBy(state -> visibleKeys.contains(state.key())));

        List<InstanceKey> toDelete = dueByVisibility.getOrDefault(false, List.of()).stream()
                .map(ProbeState::key)
                .toList();
        probeStateRepository.deleteAllById(toDelete);

        // After the removal the due states are exactly the visible ones, so leaseDue() leases only them
        Instant leaseUntil = leaseUntil(now, visible.size());
        Map<InstanceKey, ProbeState> claimed = dueByVisibility.getOrDefault(true, List.of()).stream()
                .map(state -> state.withLease(leaseUntil))
                .collect(Collectors.toMap(ProbeState::key, Function.identity()));
        probeStateRepository.leaseDue(now, leaseUntil);

        return claimed;
    }

    /**
     * Records the results in a single batch. A result is silently skipped if the version no longer matches: the
     * lease has expired mid-probe and another replica has re-claimed the instance, so its result supersedes ours.
     */
    public void recordAll(Collection<ProbeState> results) {
        if (results.isEmpty()) {
            return;
        }

        RelationalPersistentEntity<?> entity =
                jdbcConverter.getMappingContext().getRequiredPersistentEntity(ProbeState.class);

        jdbcOperations.batchUpdate(
                RECORD_RESULT,
                results.stream().map(state -> toParameters(entity, state)).toArray(MapSqlParameterSource[]::new));
    }

    private MapSqlParameterSource toParameters(RelationalPersistentEntity<?> entity, ProbeState state) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("serviceId", state.key().serviceId())
                .addValue("host", state.key().host())
                .addValue("port", state.key().port())
                .addValue("version", state.version());

        // Converted the same way Spring Data does it, e.g. Instant is stored as a String in SQLite
        bind(parameters, entity, "consecutiveFailures", state.consecutiveFailures());
        bind(parameters, entity, "lastVerdict", state.lastVerdict());
        bind(parameters, entity, "lastStatus", state.lastStatus());
        bind(parameters, entity, "nextAttemptAt", state.nextAttemptAt());
        bind(parameters, entity, "lastSuccessAt", state.lastSuccessAt());

        return parameters;
    }

    private void bind(
            MapSqlParameterSource parameters,
            RelationalPersistentEntity<?> entity,
            String property,
            @Nullable Object value) {
        RelationalPersistentProperty persistentProperty = entity.getRequiredPersistentProperty(property);
        JdbcValue jdbcValue = jdbcConverter.writeJdbcValue(
                value, persistentProperty.getTypeInformation(), jdbcConverter.getTargetSqlType(persistentProperty));

        parameters.addValue(
                property, jdbcValue.getValue(), jdbcValue.getJdbcType().getVendorTypeNumber());
    }

    /**
     * The lease has to outlive the whole run, since probes are sequential and each may take up
     * to the probe timeout. Small clusters fall back to the configured minimum.
     */
    private Instant leaseUntil(Instant now, int instanceCount) {
        Duration worstTick = properties.timeout().multipliedBy(instanceCount);
        return now.plus(max(properties.minLease(), worstTick.multipliedBy(2)));
    }

    private static Duration max(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
