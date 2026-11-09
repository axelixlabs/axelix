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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.instance.DetectedFeatures;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link InstanceProfileApi}.
 *
 * @author Mikhail Polivakha
 */
class InstanceProfileApiTest extends AbstractProtectedEndpointTest {

    private static final String PROFILE_PATH = "/api/external/instances/{instanceId}/profile";

    private final ApplicationId applicationId = ApplicationId.of("com.axelixlabs", "profile-" + UUID.randomUUID());

    private final Set<InstanceId> registered = new HashSet<>();

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @AfterEach
    void cleanup() {
        registered.forEach(registry::deRegister);
    }

    @Test
    void shouldReturnTheInstanceSiblingsCountAndDetectedFeatures() {
        // given.
        String instanceId = register("1.3.0", new DetectedFeatures(EnumSet.of(InstanceFeature.CACHES)));
        register("1.3.0", new DetectedFeatures(EnumSet.of(InstanceFeature.CACHES)));
        register("1.3.0", new DetectedFeatures(EnumSet.of(InstanceFeature.CACHES)));

        // when.
        IdentityAwareTestRestTemplate viewer =
                restTemplate.withRole(new DefaultRole("AUTHENTICATED_TEST_ROLE", Set.of()));
        ResponseEntity<String> response = viewer.getForEntity(PROFILE_PATH, String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("instance.instanceId").isEqualTo(instanceId);
        assertThatJson(response.getBody()).node("instance.commitShaShort").isEqualTo("df027cf");
        assertThatJson(response.getBody()).node("instance.status").isEqualTo("UP");
        assertThatJson(response.getBody()).node("siblingsCount").isEqualTo(2);
        assertThatJson(response.getBody()).node("features").isArray().containsExactly("caches");
        assertSuccessfulCallback(MasterWebEndpoints.INSTANCE_PROFILE_READ, viewer.getActor());
    }

    @Test
    void shouldReturnNoFeatures_whenTheInstanceDetectedNone() {
        // given.
        String instanceId = register("1.3.0", new DetectedFeatures(EnumSet.noneOf(InstanceFeature.class)));

        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(new DefaultRole("AUTHENTICATED_TEST_ROLE", Set.of()))
                .getForEntity(PROFILE_PATH, String.class, instanceId);

        // then.
        assertThatJson(response.getBody()).node("siblingsCount").isEqualTo(0);
        assertThatJson(response.getBody()).node("features").isArray().isEmpty();
    }

    @Test
    void shouldAssumeAllTheFeatures_whenTheStarterDoesNotDetectThem() {
        // given.
        String instanceId = register("1.2.0", null);

        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(new DefaultRole("AUTHENTICATED_TEST_ROLE", Set.of()))
                .getForEntity(PROFILE_PATH, String.class, instanceId);

        // then.
        assertThatJson(response.getBody())
                .node("features")
                .isArray()
                .containsExactly("caches", "scheduled-tasks", "entities-map", "transactions", "dependency-analysis");
    }

    @Test
    void shouldNotAssumeDependencyAnalysis_whenTheStarterIsOlderThanItsEndpoint() {
        // given.
        String instanceId = register("1.1.0", null);

        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(new DefaultRole("AUTHENTICATED_TEST_ROLE", Set.of()))
                .getForEntity(PROFILE_PATH, String.class, instanceId);

        // then.
        assertThatJson(response.getBody())
                .node("features")
                .isArray()
                .containsExactly("caches", "scheduled-tasks", "entities-map", "transactions");
    }

    @Test
    void shouldReturnBadRequestForAnUnknownInstance() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withRole(new DefaultRole("AUTHENTICATED_TEST_ROLE", Set.of()))
                .getForEntity(PROFILE_PATH, String.class, UUID.randomUUID());

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains(ApiErrorCodes.INSTANCE_NOT_FOUND.getErrorCode());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(
                MasterWebEndpoints.INSTANCE_PROFILE_READ,
                "/api/external/instances/00000000-0000-0000-0000-000000000001/profile"));
    }

    private String register(String starterVersion, @Nullable DetectedFeatures detectedFeatures) {
        String instanceId = UUID.randomUUID().toString();
        Instance instance =
                TestInstanceFactory.withFeatures(instanceId, applicationId, starterVersion, detectedFeatures);
        registry.register(instance);
        registered.add(instance.id());
        return instanceId;
    }
}
