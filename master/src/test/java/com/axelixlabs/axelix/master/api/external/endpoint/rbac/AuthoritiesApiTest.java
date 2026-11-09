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

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.api.external.response.AuthorityProfileResponse;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

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
    private JdbcClient jdbcClient;

    @Test
    void shouldReturnAllAuthorityProfiles() {
        // given.
        Integer count = jdbcClient
                .sql("SELECT COUNT(*) FROM authorities")
                .query(Integer.class)
                .single();
        IdentityAwareTestRestTemplate viewer = restTemplateBuilder.asUsersFeedViewer();

        // when.
        ResponseEntity<AuthorityProfileResponse[]> response =
                viewer.getForEntity(AUTHORITIES_FEED_PATH, AuthorityProfileResponse[].class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).hasSize(count);
        assertThat(response.getBody())
                .extracting(AuthorityProfileResponse::codename)
                .isSorted()
                .contains("users:read", "users:manage");
        assertThat(response.getBody()).allSatisfy(authority -> {
            assertThat(authority.id()).isNotBlank();
            assertThat(authority.displayName()).isNotBlank();
            assertThat(authority.description()).isNotBlank();
        });
        assertThat(response.getBody())
                .extracting(AuthorityProfileResponse::codename, AuthorityProfileResponse::displayName)
                .containsExactlyInAnyOrder(
                        tuple("environment.values:read", "View Environment Values"),
                        tuple("config-properties.values:read", "View Configuration Property Values"),
                        tuple("scheduled-tasks:write", "Manage Scheduled Tasks"),
                        tuple("caches:clear", "Clear Caches"),
                        tuple("caches:toggle", "Enable or Disable Caches"),
                        tuple("gc:manage", "Manage Garbage Collection"),
                        tuple("users:read", "View Users"),
                        tuple("users:manage", "Manage Users"));
        assertSuccessfulCallback(MasterWebEndpoints.AUTHORITIES_READ, viewer.getActor());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(MasterWebEndpoints.AUTHORITIES_READ, AUTHORITIES_FEED_PATH));
    }
}
