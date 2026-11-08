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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.auth.exception.TechUserAuthorizationException;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManagedServiceWebIdentityAccessManagerTest {
    private static final String SIGNING_KEY =
            "22573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff";
    private final ManagedServiceWebIdentityAccessManager subject = new ManagedServiceWebIdentityAccessManager(
            new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC512, SIGNING_KEY));
    private final DefaultTechUserJwtSerializer serializer =
            new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC512, SIGNING_KEY);

    @ParameterizedTest
    @ValueSource(strings = {"/axelix-env", "/axelix-caches/main/cache/clear"})
    void shouldAcceptMasterToken(String path) {
        // given.
        String token = serializer.serialize(TechUser.MASTER);

        // when / then.
        assertThatCode(() -> subject.verifyAccess(path, HttpMethod.DELETE, token))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectManagedServiceToken() {
        // given.
        String token = serializer.serialize(TechUser.MANAGED_SERVICE);

        // when / then.
        assertThatThrownBy(() -> subject.verifyAccess("/axelix-env", HttpMethod.GET, token))
                .isInstanceOf(TechUserAuthorizationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRejectMissingToken(String token) {
        // when / then.
        assertThatThrownBy(() -> subject.verifyAccess("/axelix-env", HttpMethod.GET, token))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        // given.
        String token = new DefaultTechUserJwtSerializer(
                        JwtAlgorithm.HMAC512,
                        "32573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff")
                .serialize(TechUser.MASTER);

        // when / then.
        assertThatThrownBy(() -> subject.verifyAccess("/axelix-env", HttpMethod.GET, token))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        // when / then.
        assertThatThrownBy(() -> subject.verifyAccess("/axelix-env", HttpMethod.GET, "malformed"))
                .isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldRejectBlankToken() {
        // when / then.
        assertThatThrownBy(() -> subject.verifyAccess("/axelix-env", HttpMethod.GET, " "))
                .isInstanceOf(JwtParsingException.class);
    }
}
