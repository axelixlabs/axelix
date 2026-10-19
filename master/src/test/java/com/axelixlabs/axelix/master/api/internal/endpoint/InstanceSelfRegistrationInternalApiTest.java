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
import java.util.EnumSet;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.master.contract.metadata.TransactionOrigin;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.features.DetectedFeatures;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.CapturingIamWebInterceptor;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link InstanceSelfRegistrationInternalApi}
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Vyacheslav Yanin
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(CapturingIamWebInterceptor.class)
public class InstanceSelfRegistrationInternalApiTest {

    private static final String TEST_INSTANCE_ID = "3c994958-924f-4a12-87d0-a8782e97af10";

    // language=json
    private static final String JSON_REQUEST = """
        {
           "basicRegistrationMetadata" : {
             "version" : "1.0.0-SNAPSHOT",
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
               "hotSpot" : {
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
               ],
               "persistenceInsights" : {
                 "transactions" : [
                   {
                     "transactionOrigin" : "APPLICATION_DECLARATIVE",
                     "transactionalKey" : {
                       "className" : "com.example.OwnerService",
                       "methodName" : "saveOwner"
                     },
                     "transactionOverallStats" : {
                       "minMs" : 1,
                       "maxMs" : 10,
                       "averageMs" : 5
                     },
                     "lazyLoadingTargets" : [ ],
                     "inMemoryPagination" : {
                       "com.example.Pet" : 2
                     }
                   }
                 ]
               },
               "scheduledTaskExecutions" : [
                 {
                   "taskId" : "com.example.OwnerJob#run()",
                   "startedAt" : "2026-09-27T10:00:00.123Z",
                   "durationMillis" : 1500,
                   "success" : false,
                   "errorType" : "NullPointerException",
                   "errorMessage" : "boom"
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

    // language=json
    private static final String JSON_HEARTBEAT_V2 = """
        {
           "instanceId" : "%s",
           "healthStatus" : "DOWN",
           "memoryDetails" : {
             "heap" : 34000
           },
           "transactions" : [
             {
               "transactionOrigin" : "APPLICATION_DECLARATIVE",
               "transactionalKey" : {
                 "className" : "com.example.VetService",
                 "methodName" : "findVets"
               },
               "transactionOverallStats" : {
                 "minMs" : 2,
                 "maxMs" : 40,
                 "averageMs" : 12
               },
               "lazyLoadingTargets" : [ ],
               "inMemoryPagination" : { },
               "externalCalls" : [ ]
             }
           ]
     }
    """.formatted(TEST_INSTANCE_ID);

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @Autowired
    private CapturingIamWebInterceptor capturingIamWebInterceptor;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.deleteAll(ScheduledTaskExecutionResult.class);
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
        capturingIamWebInterceptor.reset();
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
            assertThat(instance.starterVersion()).isEqualTo("1.0.0-SNAPSHOT");
            assertThat(instance.javaVersion()).isEqualTo(new JavaVersion(25, 0, 0, 0));
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
            // the request does not carry the detected features, as the starters older than 1.3 do
            assertThat(instance.detectedFeatures()).isNull();
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
        assertThat(snapshot.insights().persistenceInsights().getTransactions())
                .hasSize(1)
                .first()
                .satisfies(profile -> {
                    assertThat(profile.getTransactionOrigin()).isEqualTo(TransactionOrigin.APPLICATION_DECLARATIVE);
                    assertThat(profile.getTransactionalKey().getClassName()).isEqualTo("com.example.OwnerService");
                    assertThat(profile.getTransactionalKey().getMethodName()).isEqualTo("saveOwner");
                    assertThat(profile.getTransactionOverallStats().getMinMs()).isEqualTo(1);
                    assertThat(profile.getTransactionOverallStats().getMaxMs()).isEqualTo(10);
                    assertThat(profile.getTransactionOverallStats().getAverageMs())
                            .isEqualTo(5);
                    assertThat(profile.getInMemoryPagination()).containsEntry("com.example.Pet", 2);
                });

        // and then.
        assertThat(jdbcAggregateTemplate.findAll(ScheduledTaskExecutionResult.class))
                .singleElement()
                .satisfies(result -> {
                    assertThat(result.instanceId()).isEqualTo(TEST_INSTANCE_ID);
                    assertThat(result.groupId()).isEqualTo("org.springframework.samples");
                    assertThat(result.artifactId()).isEqualTo("petclinic");
                    assertThat(result.taskId()).isEqualTo("com.example.OwnerJob#run()");
                    assertThat(result.startedAt()).isEqualTo(Instant.parse("2026-09-27T10:00:00.123Z"));
                    assertThat(result.durationMillis()).isEqualTo(1500L);
                    assertThat(result.success()).isFalse();
                    assertThat(result.errorType()).isEqualTo("NullPointerException");
                    assertThat(result.errorMessage()).isEqualTo("boom");
                });

        assertThat(capturingIamWebInterceptor.accessDeniedEndpoint()).isNull();
        assertThat(capturingIamWebInterceptor.authenticationFailureEndpoint()).isNull();
        assertThat(capturingIamWebInterceptor.successfulEndpoint()).isNull();
    }

    @Test
    void shouldRegisterServiceInstanceWithEmptyGroupId() {
        // given. a Gradle service that does not declare a 'group' self-registers with an empty groupId.
        String requestWithEmptyGroupId =
                JSON_REQUEST.replace("\"groupId\" : \"org.springframework.samples\"", "\"groupId\" : \"\"");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register", defaultJsonEntity(requestWithEmptyGroupId), Void.class);

        // then. the instance is accepted and identified by its artifactId alone.
        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.NO_CONTENT);

        Optional<Instance> registeredInstance = instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID));
        assertThat(registeredInstance)
                .get()
                .extracting(Instance::applicationId)
                .isEqualTo(ApplicationId.of("", "petclinic"));

        // and then. the historical snapshot is persisted under the empty-group application id.
        var snapshot = jdbcAggregateTemplate.findById(
                new SnapshotId("", "petclinic", LocalDate.now(ZoneOffset.UTC)), HistoricalApplicationSnapshot.class);
        assertThat(snapshot).isNotNull();
    }

    @Test
    void shouldRejectRegistrationWhenStarterVersionIsOutsideTheCompatibilityWindow() {
        // given.
        String requestWithIncompatibleStarter =
                JSON_REQUEST.replace("\"version\" : \"1.0.0-SNAPSHOT\"", "\"version\" : \"1.99.0\"");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register",
                        defaultJsonEntity(requestWithIncompatibleStarter),
                        Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // and then.
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).isEmpty();
    }

    @Test
    void shouldStoreTheFeaturesDetectedByTheInstance() {
        // given. only the starters since 1.3 detect their features.
        String requestWithDetectedFeatures = JSON_REQUEST
                .replace("\"version\" : \"1.0.0-SNAPSHOT\"", "\"version\" : \"1.3.0\"")
                .replace(
                        "\"insights\" : {",
                        "\"detectedFeatures\" : [ \"caches\", \"entities-map\" ],\n\"insights\" : {");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register", defaultJsonEntity(requestWithDetectedFeatures), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID)))
                .get()
                .extracting(Instance::detectedFeatures)
                .isEqualTo(new DetectedFeatures(EnumSet.of(InstanceFeature.CACHES, InstanceFeature.ENTITIES_MAP)));
    }

    @Test
    void shouldStoreTheDependencySbomReportedOnRegistration() {
        // given.
        String sbom = CycloneDxDocuments.of(
                "org.springframework.samples:petclinic:3.5.0-SNAPSHOT", "org.slf4j:slf4j-api:2.0.9");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/register", defaultJsonEntity(requestWithSbom(sbom)), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(sbom);
    }

    @Test
    void shouldSkipSbomAndStillRegisterWhenSbomIsMalformed() {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register",
                        defaultJsonEntity(requestWithSbom("{ \"bomFormat\": \"CycloneDX\"")),
                        Void.class);

        // then. the SBOM is optional, so a broken one never costs the instance its registration.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).isPresent();
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class)).isEmpty();
    }

    @Test
    void shouldRefreshInstanceOnHeartbeatV2() {
        // given.
        restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/heartbeat/v2", defaultJsonEntity(JSON_HEARTBEAT_V2), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // and then.
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).get().satisfies(instance -> {
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.DOWN);
            assertThat(instance.memoryUsage().heap()).isEqualTo(34000.0);
            assertThat(instance.latestHeartBeat()).isNotNull();
            assertThat(instance.name()).isEqualTo("petclinic");
        });

        // and then.
        var snapshotId = new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC));
        var snapshot = jdbcAggregateTemplate.findById(snapshotId, HistoricalApplicationSnapshot.class);

        assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                .isTrue();
        assertThat(snapshot.insights().persistenceInsights().getTransactions())
                .singleElement()
                .satisfies(profile -> {
                    assertThat(profile.getTransactionalKey().getClassName()).isEqualTo("com.example.VetService");
                    assertThat(profile.getTransactionalKey().getMethodName()).isEqualTo("findVets");
                    assertThat(profile.getTransactionOverallStats().getMinMs()).isEqualTo(2);
                    assertThat(profile.getTransactionOverallStats().getMaxMs()).isEqualTo(40);
                    assertThat(profile.getTransactionOverallStats().getAverageMs())
                            .isEqualTo(12);
                });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("heartbeatsWithMissingRequiredFields")
    void shouldReturnBadRequestOnHeartbeatWithMissingRequiredField(String missingField, String heartbeat) {
        // given.
        restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/register", defaultJsonEntity(JSON_REQUEST), Void.class);
        Instant registeredHeartBeat = instanceRegistry
                .get(InstanceId.of(TEST_INSTANCE_ID))
                .orElseThrow()
                .latestHeartBeat();

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/heartbeat/v2", defaultJsonEntity(heartbeat), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // and then.
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).get().satisfies(instance -> {
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.UP);
            assertThat(instance.latestHeartBeat()).isEqualTo(registeredHeartBeat);
        });
    }

    static Stream<Arguments> heartbeatsWithMissingRequiredFields() {
        return Stream.of(
                Arguments.of(
                        "instanceId",
                        JSON_HEARTBEAT_V2.replace("\"instanceId\" : \"%s\",".formatted(TEST_INSTANCE_ID), "")),
                Arguments.of("healthStatus", JSON_HEARTBEAT_V2.replace("\"healthStatus\" : \"DOWN\",", "")),
                Arguments.of("memoryDetails", JSON_HEARTBEAT_V2.replaceAll("\"memoryDetails\" : \\{[^}]*},", "")),
                Arguments.of("heap", JSON_HEARTBEAT_V2.replace("\"heap\" : 34000", "")),
                // absent transactions default to an empty list in the generated model, only an explicit null is
                // missing.
                Arguments.of(
                        "transactions",
                        JSON_HEARTBEAT_V2.replaceAll("\"transactions\" : \\[[\\s\\S]*]", "\"transactions\" : null")));
    }

    @Test
    void shouldRejectHeartbeatAndDeRegisterInstanceWhenStarterVersionIsNotSupported() {
        // given. e.g. registered before the Master was upgraded past the starter's compatibility window
        instanceRegistry.register(TestInstanceFactory.withStarterVersion(TEST_INSTANCE_ID, "9.0.0"));

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/heartbeat/v2", defaultJsonEntity(JSON_HEARTBEAT_V2), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // and then.
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).isEmpty();
    }

    @Test
    void shouldReturnConflictOnHeartbeatOfUnknownInstance() {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/heartbeat/v2", defaultJsonEntity(JSON_HEARTBEAT_V2), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // and then.
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID))).isEmpty();
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
        assertThat(capturingIamWebInterceptor.accessDeniedEndpoint()).isNull();
        assertThat(capturingIamWebInterceptor.authenticationFailureEndpoint()).isNull();
        assertThat(capturingIamWebInterceptor.successfulEndpoint()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    void shouldReturnUnauthorizedOnHeartbeat(String scenario, TestRestTemplate testRestTemplate) {
        // when.
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                "/api/internal/service/heartbeat/v2", defaultJsonEntity(JSON_HEARTBEAT_V2), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static Stream<Arguments> invalidTokens(@Autowired TestRestTemplateBuilder builder) {
        return Stream.of(
                Arguments.of("without token", builder.withoutToken()),
                Arguments.of("expired token", builder.withExpiredTokenInAuthHeader()),
                Arguments.of("malformed token", builder.withMalformedTokenInAuthHeader()));
    }

    /**
     * @param sbom the SBOM, embedded into the request as a JSON string
     */
    private static String requestWithSbom(String sbom) {
        String escaped = sbom.replace("\"", "\\\"").replace("\n", "\\n");
        return JSON_REQUEST.replace("\"instanceId\" :", "\"sbom\" : \"%s\",\n   \"instanceId\" :".formatted(escaped));
    }

    @Test
    void shouldStoreThatTheInstanceDetectedNoFeatures() {
        // given. unlike the starters before 1.3, which do not report the field at all.
        String requestWithNoFeatures = JSON_REQUEST
                .replace("\"version\" : \"1.0.0-SNAPSHOT\"", "\"version\" : \"1.3.0\"")
                .replace("\"insights\" : {", "\"detectedFeatures\" : [ ],\n\"insights\" : {");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity("/api/internal/service/register", defaultJsonEntity(requestWithNoFeatures), Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(instanceRegistry.get(InstanceId.of(TEST_INSTANCE_ID)))
                .get()
                .extracting(Instance::detectedFeatures)
                .isEqualTo(new DetectedFeatures(EnumSet.noneOf(InstanceFeature.class)));
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}
