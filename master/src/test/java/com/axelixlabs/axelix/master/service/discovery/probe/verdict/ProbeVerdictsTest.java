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
package com.axelixlabs.axelix.master.service.discovery.probe.verdict;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProbeVerdicts}.
 *
 * @author Marsel Semenov
 */
class ProbeVerdictsTest {

    @ParameterizedTest
    @CsvSource({
        "200, REJECTED",
        "204, REJECTED",
        "301, PERMANENT",
        "302, PERMANENT",
        "400, PERMANENT",
        "401, PERMANENT",
        "403, PERMANENT",
        "404, PERMANENT",
        "405, PERMANENT",
        "408, TRANSIENT",
        "410, PERMANENT",
        "429, THROTTLED",
        "500, TRANSIENT",
        "502, TRANSIENT",
        "503, TRANSIENT",
    })
    void fromStatus_shouldClassifyStatus(int status, ProbeVerdict expected) {
        assertThat(ProbeVerdicts.fromStatus(status)).isEqualTo(expected);
    }

    @Test
    void fromStatus_shouldTreatMissingResponseAsTransient() {
        assertThat(ProbeVerdicts.fromStatus(null)).isEqualTo(ProbeVerdict.TRANSIENT);
    }

    @Test
    void parseRetryAfter_shouldParseSeconds() {
        assertThat(ProbeVerdicts.parseRetryAfter(" 120 ", Instant.now())).isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    void parseRetryAfter_shouldParseHttpDate() {
        Instant now = Instant.parse("2026-10-04T10:00:00Z");

        assertThat(ProbeVerdicts.parseRetryAfter("Sun, 04 Oct 2026 10:05:00 GMT", now))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void parseRetryAfter_shouldReturnZeroForHttpDateInThePast() {
        Instant now = Instant.parse("2026-10-04T10:00:00Z");

        assertThat(ProbeVerdicts.parseRetryAfter("Sun, 04 Oct 2026 09:00:00 GMT", now))
                .isEqualTo(Duration.ZERO);
    }

    @Test
    void parseRetryAfter_shouldReturnNullForMissingOrMalformedHeader() {
        assertThat(ProbeVerdicts.parseRetryAfter(null, Instant.now())).isNull();
        assertThat(ProbeVerdicts.parseRetryAfter(" ", Instant.now())).isNull();
        assertThat(ProbeVerdicts.parseRetryAfter("soon", Instant.now())).isNull();
    }
}
