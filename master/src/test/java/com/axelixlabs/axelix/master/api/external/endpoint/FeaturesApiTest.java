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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link FeaturesApi}.
 *
 * @author Mikhail Polivakha
 */
class FeaturesApiTest extends AbstractProtectedEndpointTest {

    private static final String instanceOn12 = UUID.randomUUID().toString();
    private static final String instanceOn11 = UUID.randomUUID().toString();

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @BeforeEach
    void prepare() {
        registry.reload(TestInstanceFactory.withStarterVersion(instanceOn12, "1.2.0"));
        registry.reload(TestInstanceFactory.withStarterVersion(instanceOn11, "1.1.4"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(instanceOn12));
        registry.deRegister(InstanceId.of(instanceOn11));
    }

    @Test
    void shouldReportTheVersionGatedFeatureForAnInstanceThatIsNewEnough() {
        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

        ResponseEntity<String> response =
                viewer.getForEntity("/api/external/features/{instanceId}", String.class, instanceOn12);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("instanceId").isEqualTo(instanceOn12);
        assertThatJson(response.getBody()).node("features").isArray().containsOnly("dependency-analysis");
        assertSuccessfulCallback(MasterWebEndpoints.FEATURES_READ, viewer.getActor());
    }

    @Test
    void shouldNotReportTheVersionGatedFeatureForAnOlderInstance() {
        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/features/{instanceId}", String.class, instanceOn11);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).node("features").isArray().isEmpty();
    }

    @Test
    void shouldReturnBadRequestForAnUnknownInstance() {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/features/{instanceId}", String.class, UUID.randomUUID());

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains(ApiErrorCodes.INSTANCE_NOT_FOUND.getErrorCode());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(
                MasterWebEndpoints.FEATURES_READ, "/api/external/features/00000000-0000-0000-0000-000000000001"));
    }
}
