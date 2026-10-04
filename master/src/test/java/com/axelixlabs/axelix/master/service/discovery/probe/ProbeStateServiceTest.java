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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.master.domain.ProbeState;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;
import com.axelixlabs.axelix.master.repository.ProbeStateRepository;
import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProbeStateService}.
 *
 * @author Marsel Semenov
 */
@DatabaseMatrixTest
class ProbeStateServiceTest {

    // Truncated, since not every database keeps sub-second precision
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    private static final InstanceKey FIRST = new InstanceKey("service", "10.0.0.1", 8080);
    private static final InstanceKey SECOND = new InstanceKey("service", "10.0.0.2", 8080);

    @Autowired
    private ProbeStateService subject;

    @Autowired
    private ProbeStateRepository probeStateRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        probeStateRepository.deleteAll();
    }

    @Test
    void registerAndClaimDue_shouldRegisterAndClaimNewInstances() {
        // when.
        Map<InstanceKey, ProbeState> claimed = subject.registerAndClaimDue(List.of(FIRST, SECOND), NOW);

        // then.
        assertThat(claimed).containsOnlyKeys(FIRST, SECOND);
        assertThat(probeStateRepository.findAll()).hasSize(2).allSatisfy(state -> {
            assertThat(state.consecutiveFailures()).isZero();
            assertThat(state.nextAttemptAt()).isAfter(NOW);
        });
    }

    @Test
    void registerAndClaimDue_shouldNotOverwriteExistingState() {
        // given.
        Instant nextAttempt = NOW.plus(1, ChronoUnit.HOURS);
        ProbeState claimed = subject.registerAndClaimDue(List.of(FIRST), NOW).get(FIRST);
        subject.recordAll(List.of(claimed.withResult(ProbeVerdict.TRANSIENT, 1, 503, nextAttempt, NOW)));

        // when.
        Map<InstanceKey, ProbeState> claimedAgain = subject.registerAndClaimDue(List.of(FIRST, SECOND), NOW);

        // then.
        assertThat(claimedAgain).containsOnlyKeys(SECOND);
        assertThat(probeStateRepository.findById(FIRST)).get().satisfies(state -> {
            assertThat(state.consecutiveFailures()).isEqualTo(1);
            assertThat(state.lastVerdict()).isEqualTo(ProbeVerdict.TRANSIENT);
            assertThat(state.lastStatus()).isEqualTo(503);
            assertThat(state.nextAttemptAt()).isEqualTo(nextAttempt);
        });
    }

    @Test
    void registerAndClaimDue_shouldNotClaimTheSameStateTwiceWhileLeased() {
        // given.
        subject.registerAndClaimDue(List.of(FIRST), NOW);

        // when.
        Map<InstanceKey, ProbeState> claimedAgain = subject.registerAndClaimDue(List.of(FIRST), NOW);

        // then.
        assertThat(claimedAgain).isEmpty();
    }

    @Test
    void registerAndClaimDue_shouldRemoveDueStatesOfDisappearedInstances() {
        // given.
        subject.registerAndClaimDue(List.of(FIRST, SECOND), NOW);

        // when.
        Instant afterLease = NOW.plus(1, ChronoUnit.HOURS);
        Map<InstanceKey, ProbeState> claimed = subject.registerAndClaimDue(List.of(FIRST), afterLease);

        // then.
        assertThat(claimed).containsOnlyKeys(FIRST);
        assertThat(probeStateRepository.findAll()).extracting(ProbeState::key).containsOnly(FIRST);
    }

    @Test
    void registerAndClaimDue_shouldKeepStatesOfDisappearedInstancesUntilDue() {
        // given.
        subject.registerAndClaimDue(List.of(FIRST, SECOND), NOW);

        // when.
        subject.registerAndClaimDue(List.of(FIRST), NOW);

        // then.
        assertThat(probeStateRepository.findAll()).extracting(ProbeState::key).containsOnly(FIRST, SECOND);
    }

    @Test
    void recordAll_shouldRecordResultsOfCurrentClaims() {
        // given.
        Map<InstanceKey, ProbeState> claimed = subject.registerAndClaimDue(List.of(FIRST, SECOND), NOW);

        // when.
        subject.recordAll(List.of(
                claimed.get(FIRST).withResult(ProbeVerdict.SUCCESS, 0, null, NOW, NOW),
                claimed.get(SECOND).withResult(ProbeVerdict.PERMANENT, 1, 404, NOW.plus(1, ChronoUnit.DAYS), NOW)));

        // then.
        assertThat(probeStateRepository.findById(FIRST)).get().satisfies(state -> {
            assertThat(state.lastVerdict()).isEqualTo(ProbeVerdict.SUCCESS);
            assertThat(state.lastSuccessAt()).isEqualTo(NOW);
            assertThat(state.nextAttemptAt()).isEqualTo(NOW);
        });
        assertThat(probeStateRepository.findById(SECOND)).get().satisfies(state -> {
            assertThat(state.lastVerdict()).isEqualTo(ProbeVerdict.PERMANENT);
            assertThat(state.lastStatus()).isEqualTo(404);
            assertThat(state.lastSuccessAt()).isNull();
        });
    }

    @Test
    void recordAll_shouldSkipResultOfStaleClaim() {
        // given.
        ProbeState stale = subject.registerAndClaimDue(List.of(FIRST), NOW).get(FIRST);

        // the lease has expired, and the instance is re-claimed by another replica
        Instant afterLease = NOW.plus(1, ChronoUnit.HOURS);
        ProbeState current =
                subject.registerAndClaimDue(List.of(FIRST), afterLease).get(FIRST);
        subject.recordAll(List.of(current.withResult(ProbeVerdict.SUCCESS, 0, null, afterLease, afterLease)));

        // when.
        subject.recordAll(List.of(stale.withResult(ProbeVerdict.PERMANENT, 1, 404, NOW.plus(1, ChronoUnit.DAYS), NOW)));

        // then.
        assertThat(probeStateRepository.findById(FIRST)).get().satisfies(state -> {
            assertThat(state.lastVerdict()).isEqualTo(ProbeVerdict.SUCCESS);
            assertThat(state.lastSuccessAt()).isEqualTo(afterLease);
        });
    }
}
