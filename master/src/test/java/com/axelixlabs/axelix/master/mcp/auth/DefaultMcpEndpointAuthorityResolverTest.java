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
package com.axelixlabs.axelix.master.mcp.auth;

import java.util.Optional;
import java.util.stream.Stream;

import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultMcpEndpointAuthorityResolver}.
 *
 * @author Mikhail Polivakha
 */
class DefaultMcpEndpointAuthorityResolverTest {

    private final DefaultMcpEndpointAuthorityResolver subject = new DefaultMcpEndpointAuthorityResolver();

    @ParameterizedTest
    @MethodSource(value = "cachesClearEndpoints")
    void shouldResolveCachesClearAuthorityForClearAllCachesEndpoint(McpEndpoint endpoint) {
        // given.
        // when.
        Optional<Authority> result = subject.resolve(endpoint);

        // then.
        assertThat(result).contains(DefaultAuthority.CACHES_CLEAR);
    }

    @Test
    void shouldReturnEmptyWhenEndpointDoesNotRequireAuthority() {
        // given.
        // when.
        Optional<Authority> result = subject.resolve(McpEndpoints.WALLBOARD);

        // then.
        assertThat(result).isEmpty();
    }

    public static Stream<Arguments> cachesClearEndpoints() {
        return Stream.of(
            Arguments.of(McpEndpoints.CLEAR_ALL_CACHES),
            Arguments.of(McpEndpoints.CLEAR_SPECIFIC_CACHE)
        );
    }
}
