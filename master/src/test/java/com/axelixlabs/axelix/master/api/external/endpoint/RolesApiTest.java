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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RolesApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(
        properties = {
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
class RolesApiTest extends AbstractProtectedEndpointTest {

    private static final String ROLES_FEED_PATH = "/api/external/roles/feed";

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanUsersTable() {
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
    }

    @Test
    void shouldReturnTheFeedOfTheBuiltInRoles() {
        // given.
        userService.createLocal("alice", null, null, "alice@example.com", null, null, "aliceSecret", "EDITOR");

        // language=json
        String expectedFeed = """
                [
                  {
                    "id": "00000000-0000-0000-0000-0000000000b1",
                    "name": "VIEWER",
                    "membersCount": 0,
                    "description": "Read-only access to the monitored applications."
                  },
                  {
                    "id": "00000000-0000-0000-0000-0000000000b2",
                    "name": "EDITOR",
                    "membersCount": 1,
                    "description": "Performs runtime operations on the monitored applications, including destructive ones such as clearing caches."
                  },
                  {
                    "id": "00000000-0000-0000-0000-0000000000b3",
                    "name": "ADMIN",
                    "membersCount": 0,
                    "description": "Everything an editor can do, plus reading sensitive configuration values."
                  }
                ]
                """;

        // when.
        IdentityAwareTestRestTemplate rolesViewer = restTemplateBuilder.asUsersFeedViewer();
        ResponseEntity<String> response = rolesViewer.getForEntity(ROLES_FEED_PATH, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedFeed);
        assertSuccessfulCallback(MasterWebEndpoints.ROLES_READ, rolesViewer.getActor());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(MasterWebEndpoints.ROLES_READ, ROLES_FEED_PATH));
    }
}
