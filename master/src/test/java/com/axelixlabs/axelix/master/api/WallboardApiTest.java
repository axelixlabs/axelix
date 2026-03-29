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
package com.axelixlabs.axelix.master.api;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.WallboardApi;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link WallboardApi}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WallboardApiTest {

    private static final String GRID_URL = "/api/external/applications/grid";

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @Nested
    class GetInstancesGrid {

        private final String instance1Id = UUID.randomUUID().toString();
        private final String instance2Id = UUID.randomUUID().toString();

        @BeforeEach
        void prepare() {
            registry.getAll().forEach(instance -> {
                registry.deRegister(instance.id());
            });

            registry.register(TestObjectFactory.createInstance(
                    instance1Id,
                    "http://example.com/1",
                    "app-one",
                    Instance.InstanceStatus.UP,
                    "21",
                    "3.4.0",
                    "6.2.0",
                    "BellSoft",
                    "2.0.0",
                    Instance.VmFeatures.empty()));

            registry.register(TestObjectFactory.createInstance(
                    instance2Id,
                    "http://example.com/2",
                    "app-two",
                    Instance.InstanceStatus.DOWN,
                    "17",
                    "2.7.0",
                    "5.3.0",
                    "Corretto",
                    null,
                    Instance.VmFeatures.empty()));
        }

        @AfterEach
        void cleanup() {
            registry.deRegister(InstanceId.of(instance1Id));
            registry.deRegister(InstanceId.of(instance2Id));
        }

        @Test
        void shouldReturnGridWithRegisteredInstances() {
            // language=json
            String expectedJson = """
                    {
                      "instances": [
                        {
                          "instanceId": "%s",
                          "name": "app-one",
                          "serviceVersion": "1.2.3-classifer-test",
                          "commitShaShort": "df027cf",
                          "status": "UP",
                          "deployedFor": "#{json-unit.ignore}",
                          "javaVersion": "21",
                          "springBootVersion": "3.4.0",
                          "springFrameworkVersion": "6.2.0",
                          "kotlinVersion": "2.0.0"
                        },
                        {
                          "instanceId": "%s",
                          "name": "app-two",
                          "serviceVersion": "1.2.3-classifer-test",
                          "commitShaShort": "df027cf",
                          "status": "DOWN",
                          "deployedFor": "#{json-unit.ignore}",
                          "javaVersion": "17",
                          "springBootVersion": "2.7.0",
                          "springFrameworkVersion": "5.3.0",
                          "kotlinVersion": null
                        }
                      ]
                    }
                    """.formatted(instance1Id, instance2Id);

            // when.
            ResponseEntity<String> response = restTemplate.withoutAuthorities().getForEntity(GRID_URL, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedJson);
        }

        @Test
        void shouldReturnEmptyGridWhenNoInstancesRegistered() {
            // given.
            registry.deRegister(InstanceId.of(instance1Id));
            registry.deRegister(InstanceId.of(instance2Id));

            // language=json
            String expectedJson = """
                    {
                      "instances": []
                    }
                    """;

            // when.
            ResponseEntity<String> response = restTemplate.withoutAuthorities().getForEntity(GRID_URL, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody()).isEqualTo(expectedJson);
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response =
                scenario.getModifier().apply(restTemplate).getForEntity(GRID_URL, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
