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
package com.axelixlabs.axelix.master.service.discovery.probe.backoff;

import java.time.Duration;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile;
import com.axelixlabs.axelix.master.service.discovery.probe.ProbeOutcome;
import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProbeBackoff}.
 *
 * @author Marsel Semenov
 */
class ProbeBackoffTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");

    private final ProbeBackoff subject = new ProbeBackoff(new ProbeBackoffProperties(
            Duration.ofSeconds(5),
            Duration.ofMinutes(1),
            Duration.ofMinutes(1),
            Duration.ofMinutes(30),
            Duration.ofMinutes(5),
            Duration.ofHours(1),
            Duration.ofHours(1),
            5));

    @Test
    void nextAttempt_shouldProbeAgainRightAwayAfterSuccess() {
        ProbeOutcome success = new ProbeOutcome.Success(Mockito.mock(DiscoveredInstanceProfile.class));

        assertThat(subject.nextAttempt(success, 0, NOW)).isEqualTo(NOW);
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "2, 2", "3, 4", "5, 16", "6, 30", "100, 30"})
    void nextAttempt_shouldGrowExponentiallyUpToCapForTransientFailures(int failures, long expectedMinutes) {
        assertThat(subject.nextAttempt(failure(ProbeVerdict.TRANSIENT, null), failures, NOW))
                .isEqualTo(NOW.plus(Duration.ofMinutes(expectedMinutes)));
    }

    @Test
    void nextAttempt_shouldHonorRetryAfterWhenThrottled() {
        assertThat(subject.nextAttempt(failure(ProbeVerdict.THROTTLED, Duration.ofMinutes(2)), 1, NOW))
                .isEqualTo(NOW.plus(Duration.ofMinutes(2)));
    }

    @Test
    void nextAttempt_shouldFallBackToDefaultWhenThrottledWithoutRetryAfter() {
        assertThat(subject.nextAttempt(failure(ProbeVerdict.THROTTLED, null), 1, NOW))
                .isEqualTo(NOW.plus(Duration.ofMinutes(5)));
    }

    @Test
    void nextAttempt_shouldCapRetryAfterWhenThrottled() {
        assertThat(subject.nextAttempt(failure(ProbeVerdict.THROTTLED, Duration.ofDays(1)), 1, NOW))
                .isEqualTo(NOW.plus(Duration.ofHours(1)));
    }

    @ParameterizedTest
    @CsvSource({"PERMANENT", "REJECTED"})
    void nextAttempt_shouldWaitLongForPermanentFailures(ProbeVerdict verdict) {
        assertThat(subject.nextAttempt(failure(verdict, null), 1, NOW)).isEqualTo(NOW.plus(Duration.ofHours(1)));
    }

    private static ProbeOutcome.Failure failure(ProbeVerdict verdict, @Nullable Duration retryAfter) {
        return new ProbeOutcome.Failure(verdict, null, retryAfter, "test");
    }
}
