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
package com.axelixlabs.axelix.master.api.internal.endpoint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link HeartBeatApi}
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HeartBeatApiTest {

    private static final String TEST_INSTANCE_ID = "3c994958-924f-4a12-87d0-a8782e97af10";

    // language=json
    private static final String JSON_REQUEST = """
        {
           "basicRegistrationMetadata" : {
             "version": "1.0.0-SNAPSHOT",
             "serviceVersion" : "3.5.0-SNAPSHOT",
             "groupId" : "org.springframework.samples",
             "artifactId" : "petclinic",
             "commitShortSha" : "a8b0929",
             "jdkVendor" : "BellSoft",
             "gcInUse" : "G1",
             "softwareVersions" : {
               "springBoot" : "3.5.0",
               "java" : "25",
               "springFramework" : "6.1.2",
               "kotlin" : null
             },
             "healthStatus" : "UP",
             "memoryDetails" : {
               "heap" : 12000
             },
             "insights" : {
               "hotSpotInsights" : {
                 "projectLeyden" : [
                   {
                     "featureId" : "AotCache",
                     "enabled" : false
                   },
                   {
                     "featureId" : "AppCDS",
                     "enabled" : true
                   }
                 ],
                 "gc" : [
                   {
                     "featureId" : "GCLoggingEnabled",
                     "enabled" : false
                   },
                   {
                     "featureId" : "GCLogFileSpecified",
                     "enabled" : false
                   }
                 ],
                 "projectLilliputh" : [
                   {
                     "featureId" : "CompactObjectHeaders",
                     "enabled" : false
                   }
                 ]
               },
               "springFramework" : [
                 {
                   "featureId" : "OSIV",
                   "enabled" : true
                 }
               ]
             }
           },
           "instanceId" : "%s",
           "instanceName" : "petclinic",
           "instanceActuatorUrl" : "http://localhost:8080/actuator",
           "deploymentAt" : "2025-02-03T13:29:29Z"
     }
    """.formatted(TEST_INSTANCE_ID);

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @Test
    void shouldRegisterServiceInstance() {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);

        // then.
        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.NO_CONTENT);

        // and then.
        Optional<Instance> registeredInstance = instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID));
        assertThat(registeredInstance).get().satisfies(instance -> {
            assertThat(instance.id().instanceId()).isEqualTo(TEST_INSTANCE_ID);
            assertThat(instance.applicationId())
                    .isEqualTo(ApplicationId.of("org.springframework.samples", "petclinic"));
            assertThat(instance.name()).isEqualTo("petclinic");
            assertThat(instance.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
            assertThat(instance.javaVersion()).isEqualTo("25");
            assertThat(instance.springBootVersion()).isEqualTo("3.5.0");
            assertThat(instance.springFrameworkVersion()).isEqualTo("6.1.2");
            assertThat(instance.kotlinVersion()).isNull();
            assertThat(instance.jdkVendor()).isEqualTo("BellSoft");
            assertThat(instance.commitShaShort()).isEqualTo("a8b0929");
            assertThat(instance.deployedAt()).isEqualTo(Instant.parse("2025-02-03T13:29:29Z"));
            assertThat(instance.latestHeartBeat()).isNotNull();
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.UP);
            assertThat(instance.memoryUsage().heap()).isEqualTo(12000.0);
            assertThat(instance.actuatorUrl()).isEqualTo("http://localhost:8080/actuator");
        });

        var snapshotId = new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC));
        var snapshot = jdbcAggregateTemplate.findById(snapshotId, HistoricalApplicationSnapshot.class);

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                .isTrue();
        assertThat(snapshot.insights().hotSpot().projectLeyden().aotCacheEnabled())
                .isFalse();
        assertThat(snapshot.insights().hotSpot().gc().gcLoggingEnabled()).isFalse();
        assertThat(snapshot.insights().hotSpot().gc().gcInUse()).isEqualTo(GarbageCollector.G1);
        assertThat(snapshot.insights().hotSpot().projectLilliput().compactObjectHeadersEnabled())
                .isFalse();
        assertThat(snapshot.insights().springFramework().osivEnabled()).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    void shouldReturnUnauthorized(String scenario, TestRestTemplate testRestTemplate) {
        // when.
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                "/api/internal/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // and then.
        Optional<Instance> registeredInstance = instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID));
        assertThat(registeredInstance).isEmpty();
    }

    private static Stream<Arguments> invalidTokens(@Autowired TestRestTemplateBuilder builder) {
        return Stream.of(
                Arguments.of("without token", builder.withoutToken()),
                Arguments.of("expired token", builder.withExpiredTokenInAuthHeader()),
                Arguments.of("malformed token", builder.withMalformedTokenInAuthHeader()));
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}
