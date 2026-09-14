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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DependenciesApi}.
 *
 * @author Mikhail Polivakha
 */
class DependenciesApiTest extends AbstractProtectedEndpointTest {

    /**
     * The analysis of the {@code axelix/sbom/dependencies.cdx.json} fixture: a diamond where both
     * {@code spring-webmvc} and {@code spring-cloud-starter-sleuth} pull in {@code spring-core}, so the resolution
     * path of {@code spring-core} follows the first discovered chain. The {@code oldestSupportedLine} of the
     * framework depends on the wall clock and is ignored below, same as {@code analyzedAt}.
     */
    // language=json
    private static final String EXPECTED_ANALYSIS_JSON = """
    {
      "rootCoordinates": "com.axelixlabs:playground-app:1.0.0",
      "framework": {
        "name": "SPRING_BOOT",
        "version": "3.5.2",
        "line": {
          "line": "3.5.x",
          "releasedAt": "2025-05-22",
          "ossSupportEndsAt": "2026-06-30",
          "commercialSupportEndsAt": "2032-06-30"
        },
        "latestKnownLine": {
          "line": "4.1.x",
          "releasedAt": "2026-06-30",
          "ossSupportEndsAt": "2027-07-31",
          "commercialSupportEndsAt": "2028-07-31"
        }
      },
      "dependencies": [
        {
          "dependency": {
            "library": { "groupId": "org.springframework", "artifactId": "spring-webmvc" },
            "version": "6.1.5"
          },
          "resolutionPath": [ "org.springframework:spring-webmvc:6.1.5" ],
          "softwareProject": null
        },
        {
          "dependency": {
            "library": { "groupId": "org.springframework.cloud", "artifactId": "spring-cloud-starter-sleuth" },
            "version": "3.1.11"
          },
          "resolutionPath": [ "org.springframework.cloud:spring-cloud-starter-sleuth:3.1.11" ],
          "softwareProject": null
        },
        {
          "dependency": {
            "library": { "groupId": "org.springframework", "artifactId": "spring-core" },
            "version": "6.1.5"
          },
          "resolutionPath": [
            "org.springframework:spring-webmvc:6.1.5",
            "org.springframework:spring-core:6.1.5"
          ],
          "softwareProject": null
        }
      ]
    }
    """;

    private static final String instanceWithSbom = UUID.randomUUID().toString();
    private static final String instanceWithoutSbom = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        String sbom = readFixture();

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + instanceWithSbom + "/actuator/axelix-dependencies")) {
                    return new MockResponse().setBody(sbom).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + instanceWithoutSbom + "/actuator/axelix-dependencies")) {
                    // the managed application was built without an Axelix build plugin
                    return new MockResponse().setResponseCode(204);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.reload(
                TestInstanceFactory.create(instanceWithSbom, mockWebServer.url(instanceWithSbom) + "/actuator"));
        registry.reload(
                TestInstanceFactory.create(instanceWithoutSbom, mockWebServer.url(instanceWithoutSbom) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(instanceWithSbom));
        registry.deRegister(InstanceId.of(instanceWithoutSbom));
    }

    @Test
    void shouldReturnTheDependencyAnalysis() {
        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

        ResponseEntity<String> response =
                viewer.getForEntity("/api/external/dependencies/{instanceId}", String.class, instanceWithSbom);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody())
                .whenIgnoringPaths("analyzedAt", "framework.oldestSupportedLine")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(EXPECTED_ANALYSIS_JSON);
        assertSuccessfulCallback(MasterWebEndpoints.DEPENDENCIES_READ, viewer.getActor());
    }

    @Test
    void shouldReturnNotFoundWhenTheInstanceServesNoSbom() {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/dependencies/{instanceId}", String.class, instanceWithoutSbom);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains(ApiErrorCodes.SBOM_NOT_AVAILABLE.getErrorCode());
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/dependencies/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(
                MasterWebEndpoints.DEPENDENCIES_READ,
                "/api/external/dependencies/00000000-0000-0000-0000-000000000001"));
    }

    private static String readFixture() {
        try {
            return new String(
                    new ClassPathResource("axelix/sbom/dependencies.cdx.json")
                            .getInputStream()
                            .readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
