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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.OssAuthority;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthoritiesManager;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StarterJwtEncoderService}.
 *
 * @author Mikhail Polivakha
 */
class StarterJwtEncoderServiceTest {

    private static final String SIGNING_KEY = "0123456789abcdef0123456789abcdef";

    private static final Duration LIFESPAN = Duration.ofHours(1);

    @Test
    void starterTokenRoundTripsThroughTheSharedDecoder() {
        Role component = new DefaultRole("VIEWER", Set.of(OssAuthority.ENV_VALUES_READ));
        Role admin = new DefaultRole("ADMIN", Set.of(OssAuthority.CACHES_CLEAR), Set.of(component));
        DefaultUser user = new DefaultUser("user-1", "alice", null, Set.of(admin));

        StarterJwtEncoderService encoder =
                new StarterJwtEncoderService(JwtAlgorithm.HMAC256, SIGNING_KEY, LIFESPAN);
        DefaultJwtDecoderService decoder = new DefaultJwtDecoderService(
                new DefaultAuthoritiesManager(null), JwtAlgorithm.HMAC256, SIGNING_KEY);

        PasswordlessUser decoded = decoder.decodeTokenToUser(encoder.generateToken(user));

        assertThat(decoded.getId()).isEqualTo("user-1");
        assertThat(decoded.getUsername()).isEqualTo("alice");
        assertThat(decoded.getRoles()).hasSize(1);

        Role decodedAdmin = decoded.getRoles().iterator().next();
        assertThat(decodedAdmin.getName()).isEqualTo("ADMIN");
        assertThat(decodedAdmin.getAuthorities()).containsExactly(OssAuthority.CACHES_CLEAR);
        assertThat(decodedAdmin.getComponents()).hasSize(1);

        Role decodedViewer = decodedAdmin.getComponents().iterator().next();
        assertThat(decodedViewer.getName()).isEqualTo("VIEWER");
        assertThat(decodedViewer.getAuthorities()).containsExactly(OssAuthority.ENV_VALUES_READ);
    }
}
