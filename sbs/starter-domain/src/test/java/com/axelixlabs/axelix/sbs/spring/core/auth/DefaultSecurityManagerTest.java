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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.common.auth.JwtDecoderService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultSecurityManager}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(MockitoExtension.class)
class DefaultSecurityManagerTest {

    @Mock
    private JwtDecoderService jwtDecoderService;

    @Mock
    private AuthorityResolver authorityResolver;

    @Mock
    private Authorizer authorizer;

    private DefaultSecurityManager securityManager() {
        return new DefaultSecurityManager(jwtDecoderService, authorityResolver, authorizer);
    }

    @Nested
    class ShouldAuthorize {

        @Test
        void returnsTrue_whenPathStartsWithActuatorAxelix() {
            // given.
            DefaultSecurityManager manager = securityManager();
            // when.
            boolean result = manager.shouldAuthorize("/actuator/axelix-beans");
            // then.
            assertThat(result).isTrue();
        }

        @Test
        void returnsFalse_whenPathDoesNotStartWithActuatorAxelix() {
            // given.
            DefaultSecurityManager manager = securityManager();
            // when.
            boolean result = manager.shouldAuthorize("/actuator/health");
            // then.
            assertThat(result).isFalse();
        }
    }
}
