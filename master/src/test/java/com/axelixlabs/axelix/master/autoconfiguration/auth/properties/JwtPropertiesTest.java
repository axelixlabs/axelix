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
package com.axelixlabs.axelix.master.autoconfiguration.auth.properties;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtProperties}.
 *
 * @author Mikhail Polivkha
 */
class JwtPropertiesTest {

    private static final String VALID_SIGNING_KEY =
            "22573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff";

    @Test
    void shouldAcceptValidSigningKey() {
        // when. // then.
        assertThatCode(() -> new JwtProperties(JwtAlgorithm.HMAC512, VALID_SIGNING_KEY, Duration.ofHours(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInsufficientlyShortSigningKey() {
        // when. // then.
        assertThatThrownBy(() -> new JwtProperties(JwtAlgorithm.HMAC512, "secret", Duration.ofHours(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
