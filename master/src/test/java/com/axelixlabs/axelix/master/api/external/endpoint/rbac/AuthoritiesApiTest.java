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
package com.axelixlabs.axelix.master.api.external.endpoint.rbac;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.api.external.response.UserAuthoritiesResponse;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AuthoritiesApi}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
class AuthoritiesApiTest extends AbstractProtectedEndpointTest {

    private static final String AUTHORITIES_FEED_PATH = "/api/external/authorities/feed";

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRequireUserId() {
        // given.
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();

        // when.
        ResponseEntity<String> response = viewer.getForEntity(AUTHORITIES_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnUserAuthorityPartition() {
        // given.
        String username = "authority-feed-" + UUID.randomUUID();
        userService.createLocal(username, null, null, null, "password", Set.of("EDITOR"));
        String userId = userRepository.findByUsername(username).orElseThrow().id();
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();

        try {
            // when.
            ResponseEntity<UserAuthoritiesResponse> response =
                    viewer.getForEntity(AUTHORITIES_FEED_PATH + "?userId=" + userId, UserAuthoritiesResponse.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThat(response.getBody().effectiveAuthorities())
                    .extracting(UserAuthoritiesResponse.AuthorityProfileResponse::codename)
                    .isSorted()
                    .containsExactly("caches:clear", "caches:toggle", "gc:manage", "scheduled-tasks:write");
            assertThat(response.getBody().ungrantedAuthorities())
                    .extracting(UserAuthoritiesResponse.AuthorityProfileResponse::codename)
                    .isSorted()
                    .containsExactly(
                            "config-properties.values:read", "environment.values:read", "users:manage", "users:read");
            assertSuccessfulCallback(MasterWebEndpoints.AUTHORITIES_READ, viewer.getActor());
        } finally {
            userService.deleteByIds(List.of(userId));
        }
    }

    @Test
    void shouldReturnNotFoundForMissingUser() {
        // given.
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();

        // when.
        ResponseEntity<String> response = viewer.getForEntity(AUTHORITIES_FEED_PATH + "?userId=missing", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(
                MasterWebEndpoints.AUTHORITIES_READ, AUTHORITIES_FEED_PATH + "?userId=missing"));
    }
}
