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
package com.axelixlabs.axelix.master.service.discovery;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import io.fabric8.kubernetes.client.KubernetesClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.instance.DetectedFeatures;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.domain.instance.InstanceSbom;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link ShortPollingInstanceDiscoveryScheduler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Vyacheslav Yanin
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"axelix.master.discovery.auto.enabled=true", "axelix.master.discovery.auto.platform=kubernetes"})
class ShortPollingInstanceDiscoverySchedulerTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ShortPollingInstanceDiscoveryScheduler subject;

    @MockitoBean
    private KubernetesClient kubernetesClient;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    private KubernetesDiscoveryStubs stubs;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    void setUp() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.close();
        }
        // TODO: Starting up a new web server on every test? Pretty overpowered for that...
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        jdbcAggregateTemplate.deleteAll(ScheduledTaskExecutionResult.class);
        instanceRepository.deleteAll();
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
        stubs = new KubernetesDiscoveryStubs(discoveryClient, mockWebServer);
    }

    @TestConfiguration
    static class CurrentConfiguration {

        @Bean
        @Primary
        public AxelixVersionDiscoverer testAxelixVersionDiscoverer() {
            // Within the compatibility window of both the 1.0.0-SNAPSHOT starters most tests use and the newest one
            return () -> "1.3.0-SNAPSHOT";
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldRegisterNewK8sInstancesWhenDiscovered() {
        String service1 = "service-1";
        String service2 = "service-2";
        String instance1Id = UUID.randomUUID().toString();
        String instance2Id = UUID.randomUUID().toString();

        stubs.enqueueResponses(
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "app1")),
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "app2")));
        stubs.stubDiscovery(stubs.k8sInstance(service1, instance1Id), stubs.k8sInstance(service2, instance2Id));

        // when.
        subject.performDiscovery();

        // then.
        List<Instance> registeredInstances = instanceRegistry.getAll();
        assertThat(registeredInstances).hasSize(2);

        assertThat(registeredInstances).extracting(it -> it.id().instanceId()).containsOnly(instance1Id, instance2Id);

        // and then.
        assertThat(jdbcAggregateTemplate.findAll(ScheduledTaskExecutionResult.class))
                .isEmpty();
    }

    @Test
    void shouldSaveScheduledTaskExecutionsWhenInstancesAreDiscovered() {
        String serviceWithFailedExecution = "service-with-failed-execution";
        String serviceWithSuccessfulExecution = "service-with-successful-execution";
        String failedInstanceId = UUID.randomUUID().toString();
        String successfulInstanceId = UUID.randomUUID().toString();
        String groupId = "org.springframework.samples";
        String artifactId = "petclinic";
        String failedTaskId = "com.example.OwnerJob#run()";
        String successfulTaskId = "com.example.OwnerArchivistTask#archive()";

        BasicRegistrationMetadata failedMetadata = TestMetadataFactory.create(groupId, artifactId);
        failedMetadata
                .getInsights()
                .scheduledTaskExecutions(List.of(new ScheduledTaskExecution()
                        .taskId(failedTaskId)
                        .startedAt("2026-09-27T10:00:00.123Z")
                        .durationMillis(1500L)
                        .success(false)
                        .launchType(ScheduledTaskLaunchType.SCHEDULED)
                        .errorType("NullPointerException")
                        .errorMessage("boom")));

        BasicRegistrationMetadata successfulMetadata = TestMetadataFactory.create(groupId, artifactId);
        successfulMetadata
                .getInsights()
                .scheduledTaskExecutions(List.of(new ScheduledTaskExecution()
                        .taskId(successfulTaskId)
                        .startedAt("2026-09-27T10:05:00.500Z")
                        .durationMillis(42L)
                        .success(true)
                        .launchType(ScheduledTaskLaunchType.MANUAL)));

        stubs.enqueueResponses(
                TestMetadataFactory.toJson(failedMetadata), TestMetadataFactory.toJson(successfulMetadata));
        stubs.stubDiscovery(
                stubs.k8sInstance(serviceWithFailedExecution, failedInstanceId),
                stubs.k8sInstance(serviceWithSuccessfulExecution, successfulInstanceId));

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(2);

        // and then.
        assertThat(jdbcAggregateTemplate.findAll(ScheduledTaskExecutionResult.class))
                .hasSize(2)
                .extracting(
                        ScheduledTaskExecutionResult::instanceId,
                        ScheduledTaskExecutionResult::taskId,
                        ScheduledTaskExecutionResult::startedAt,
                        ScheduledTaskExecutionResult::durationMillis,
                        ScheduledTaskExecutionResult::success,
                        ScheduledTaskExecutionResult::launchType,
                        ScheduledTaskExecutionResult::errorType,
                        ScheduledTaskExecutionResult::errorMessage)
                .containsExactlyInAnyOrder(
                        tuple(
                                failedInstanceId,
                                failedTaskId,
                                Instant.parse("2026-09-27T10:00:00.123Z"),
                                1500L,
                                false,
                                ScheduledTaskLaunchType.SCHEDULED,
                                "NullPointerException",
                                "boom"),
                        tuple(
                                successfulInstanceId,
                                successfulTaskId,
                                Instant.parse("2026-09-27T10:05:00.500Z"),
                                42L,
                                true,
                                ScheduledTaskLaunchType.MANUAL,
                                null,
                                null));

        // and then.
        assertThat(jdbcAggregateTemplate.findAll(ScheduledTaskExecutionResult.class))
                .extracting(ScheduledTaskExecutionResult::groupId, ScheduledTaskExecutionResult::artifactId)
                .containsOnly(tuple(groupId, artifactId));
    }

    @Test
    void shouldReplaceInstancesWhenDiscovered() {
        String service = "service-3";
        String instanceId = UUID.randomUUID().toString();

        String firstResponse =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        BasicRegistrationMetadata secondMetadata = TestMetadataFactory.create(
                        "org.springframework.samples", "petclinic")
                .commitShortSha("910230")
                .healthStatus(HealthStatus.DOWN);
        secondMetadata.getSoftwareVersions().springBoot("3.5.2");
        String secondResponse = TestMetadataFactory.toJson(secondMetadata);

        stubs.enqueueResponses(firstResponse, secondResponse);
        stubs.stubDiscovery(stubs.k8sInstance(service, instanceId));
        subject.performDiscovery();

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(1).allSatisfy(instance -> {
            assertThat(instance.id().instanceId()).isEqualTo(instanceId);
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.DOWN);
            assertThat(instance.commitShaShort()).isEqualTo("910230");
            assertThat(instance.springBootVersion()).isEqualTo("3.5.2");
        });
    }

    @Test
    void shouldSaveScheduledTaskExecutionsReportedViaMasterPing() {
        String instanceId = UUID.randomUUID().toString();
        ScheduledTaskExecution reportedOnRegistration = new ScheduledTaskExecution()
                .taskId("com.example.OwnerJob#run()")
                .startedAt("2026-09-27T10:00:00.123Z")
                .durationMillis(1500L)
                .success(true)
                .launchType(ScheduledTaskLaunchType.SCHEDULED);

        BasicRegistrationMetadata metadata = TestMetadataFactory.create("org.springframework.samples", "petclinic")
                .version("1.3.0");
        metadata.getInsights().scheduledTaskExecutions(List.of(reportedOnRegistration));

        // language=json
        String masterPingResponse = """
            {
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 500
              },
              "transactions" : [ ],
              "scheduledTaskExecutions" : [
                {
                  "taskId" : "com.example.OwnerJob#run()",
                  "startedAt" : "2026-09-27T10:00:00.123Z",
                  "durationMillis" : 1500,
                  "success" : true,
                  "launchType" : "SCHEDULED"
                },
                {
                  "taskId" : "com.example.OwnerJob#run()",
                  "startedAt" : "2026-09-27T10:01:00.456Z",
                  "durationMillis" : 700,
                  "success" : true,
                  "launchType" : "MANUAL"
                }
              ]
            }
            """;

        stubs.enqueueResponses(TestMetadataFactory.toJson(metadata), masterPingResponse);
        stubs.stubDiscovery(stubs.k8sInstance("service-with-master-ping", instanceId));
        subject.performDiscovery();

        // when.
        subject.performDiscovery();

        // then.
        assertThat(jdbcAggregateTemplate.findAll(ScheduledTaskExecutionResult.class))
                .extracting(
                        ScheduledTaskExecutionResult::instanceId,
                        ScheduledTaskExecutionResult::startedAt,
                        ScheduledTaskExecutionResult::launchType)
                .containsExactlyInAnyOrder(
                        tuple(instanceId, Instant.parse("2026-09-27T10:00:00.123Z"), ScheduledTaskLaunchType.SCHEDULED),
                        tuple(instanceId, Instant.parse("2026-09-27T10:01:00.456Z"), ScheduledTaskLaunchType.MANUAL));
    }

    @Test
    void shouldRefreshKnownInstanceViaMasterPingEndpoint() throws InterruptedException {
        String service = "service-with-master-ping";
        String instanceId = UUID.randomUUID().toString();

        String metadataResponse =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")
                        .version("1.3.0"));

        // language=json
        String masterPingResponse = """
            {
              "healthStatus" : "DOWN",
              "memoryDetails" : {
                "heap" : 500
              },
              "transactions" : [
                {
                  "transactionOrigin" : "APPLICATION_DECLARATIVE",
                  "transactionalKey" : {
                    "className" : "org.springframework.samples.petclinic.OwnerService",
                    "methodName" : "findOwners"
                  },
                  "transactionOverallStats" : {
                    "minMs" : 1,
                    "maxMs" : 3,
                    "averageMs" : 2
                  },
                  "lazyLoadingTargets" : [ ],
                  "inMemoryPagination" : { },
                  "externalCalls" : [ ]
                }
              ]
            }
            """;

        stubs.enqueueResponses(metadataResponse, masterPingResponse);
        stubs.stubDiscovery(stubs.k8sInstance(service, instanceId));
        subject.performDiscovery();

        // when.
        subject.performDiscovery();

        // then.
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-metadata");
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-ping");

        assertThat(instanceRegistry.getAll()).singleElement().satisfies(instance -> {
            assertThat(instance.id().instanceId()).isEqualTo(instanceId);
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.DOWN);
            assertThat(instance.memoryUsage().heap()).isEqualTo(500);
            assertThat(instance.commitShaShort()).isEqualTo("a8b0929");
            assertThat(instance.latestHeartBeat()).isNull();
        });

        HistoricalApplicationSnapshot snapshot = jdbcAggregateTemplate.findById(
                new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC)),
                HistoricalApplicationSnapshot.class);

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.insights().persistenceInsights().getTransactions())
                .singleElement()
                .satisfies(transaction -> assertThat(
                                transaction.getTransactionalKey().getMethodName())
                        .isEqualTo("findOwners"));
    }

    @Test
    void shouldStoreSbomOnlyWhenInstanceIsRegistered() throws InterruptedException {
        // given.
        String service = "service-with-sbom";
        String instanceId = UUID.randomUUID().toString();
        String sbom = CycloneDxDocuments.of(
                "org.springframework.samples:petclinic:1.0.0",
                "org.springframework.cloud:spring-cloud-starter-sleuth:3.1.11");

        String metadataResponse =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")
                        .version("1.3.0")
                        .sbom(sbom));

        // language=json
        String masterPingResponse = """
            {
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 500
              },
              "transactions" : [ ]
            }
            """;

        stubs.enqueueResponses(metadataResponse, masterPingResponse);
        stubs.stubDiscovery(stubs.k8sInstance(service, instanceId));

        // when.
        subject.performDiscovery();

        // then.
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .satisfies(dependencies -> {
                    assertThat(dependencies.applicationId().groupId()).isEqualTo("org.springframework.samples");
                    assertThat(dependencies.applicationId().artifactId()).isEqualTo("petclinic");
                    assertThat(dependencies.sbom()).isEqualTo(sbom);
                });
        Instant reportedAt = jdbcAggregateTemplate
                .findAll(ApplicationDependencies.class)
                .getFirst()
                .reportedAt();

        // when. the instance is already known, so it is only refreshed via the master ping.
        subject.performDiscovery();

        // then. the SBOM was neither fetched nor stored again.
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-metadata");
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-ping");
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .satisfies(dependencies -> assertThat(dependencies.reportedAt()).isEqualTo(reportedAt));
        // and the SBOM of the instance is kept, even though nothing has reported it again.
        assertThat(jdbcAggregateTemplate.findById(InstanceId.of(instanceId), InstanceSbom.class))
                .isNotNull()
                .extracting(InstanceSbom::sbom)
                .isEqualTo(sbom);
    }

    @Test
    void shouldDeregisterK8sInstanceWhenNoLongerInDiscovery() {
        String serviceId = "test-service";
        String instanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.respondOn("/actuator/axelix-metadata", response);

        ServiceInstance k8sServiceInstance = stubs.k8sInstance(serviceId, instanceId);

        Mockito.when(discoveryClient.getServices())
                .thenReturn(List.of(serviceId))
                .thenReturn(List.of());

        Mockito.when(discoveryClient.getInstances(serviceId)).thenReturn(List.of(k8sServiceInstance));

        subject.performDiscovery();

        assertThat(instanceRegistry.getAll()).hasSize(1);

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).isEmpty();
    }

    @Test
    void shouldKeepSelfRegisteredInstance() {
        // language=json
        String selfRegistrationRequest = """
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
               "hotSpot" : {
                 "projectLeyden" : [ ],
                 "gc" : [ ],
                 "projectLilliputh" : [ ]
               },
               "springFramework" : [ ],
               "persistenceInsights" : {
                 "transactions" : [ ]
               }
             }
           },
             "instanceId" : "3c994958-924f-4a12-87d0-a8782e97af10",
             "instanceName" : "petclinic",
             "instanceActuatorUrl" : "http://localhost:8080/actuator",
             "deploymentAt" : "2025-02-03T13:29:29Z"
         }
        """;

        restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register", defaultJsonEntity(selfRegistrationRequest), Void.class);

        assertThat(instanceRegistry.getAll()).hasSize(1);

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(1);
    }

    @Test
    void shouldKeepSelfRegisteredAndDeregisterK8sInstance() {
        // language=json
        String selfRegistrationRequest = """
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
               "hotSpot" : {
                 "projectLeyden" : [ ],
                 "gc" : [ ],
                 "projectLilliputh" : [ ]
               },
               "springFramework" : [ ],
               "persistenceInsights" : {
                 "transactions" : [ ]
               }
             }
           },
             "instanceId" : "3c994958-924f-4a12-87d0-a8782e97af10",
             "instanceName" : "petclinic",
             "instanceActuatorUrl" : "http://localhost:8080/actuator",
             "deploymentAt" : "2025-02-03T13:29:29Z"
         }
        """;

        restTemplate
                .withRoleTokenInAuthorizationHeader(DefaultRole.MANAGED_SERVICE)
                .postForEntity(
                        "/api/internal/service/register", defaultJsonEntity(selfRegistrationRequest), Void.class);

        String serviceId = "test-service";
        String instanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.respondOn("/actuator/axelix-metadata", response);

        ServiceInstance k8sServiceInstance = stubs.k8sInstance(serviceId, instanceId);

        Mockito.when(discoveryClient.getServices())
                .thenReturn(List.of(serviceId)) // discovered at first
                .thenReturn(List.of()); // and then not disocovered

        Mockito.when(discoveryClient.getInstances(serviceId)).thenReturn(List.of(k8sServiceInstance));

        subject.performDiscovery();

        assertThat(instanceRegistry.getAll()).hasSize(2);

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll())
                .hasSize(1)
                .first()
                .extracting(instance -> instance.id().instanceId())
                .isEqualTo("3c994958-924f-4a12-87d0-a8782e97af10");
    }

    @Test
    void shouldUpdateHistoricalApplicationSnapshotWhenInstanceIsDiscovered() {
        String service = "service-with-insights";
        String instanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.enqueueResponses(response);
        stubs.stubDiscovery(stubs.k8sInstance(service, instanceId));

        // when.
        subject.performDiscovery();

        // then.
        SnapshotId snapshotId =
                new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC));
        HistoricalApplicationSnapshot snapshot =
                jdbcAggregateTemplate.findById(snapshotId, HistoricalApplicationSnapshot.class);

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                .isTrue();
        assertThat(snapshot.insights().hotSpot().projectLeyden().aotCacheEnabled())
                .isFalse();
        assertThat(snapshot.insights().hotSpot().gc().gcLoggingEnabled()).isFalse();
        assertThat(snapshot.insights().hotSpot().projectLilliput().compactObjectHeadersEnabled())
                .isFalse();
        assertThat(snapshot.insights().springFramework().osivEnabled()).isTrue();
        assertThat(snapshot.insights().persistenceInsights().getTransactions()).isEmpty();
    }

    @Test
    void shouldHandleEmptyDiscoveryResponse() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).isEmpty();
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @Test
    void shouldStoreThatTheDiscoveredInstanceDetectedNoFeatures() {
        // given. unlike the starters before 1.3, which do not report the field at all.
        String service = "petclinic";
        String instanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")
                        .version("1.3.0")
                        .detectedFeatures(List.of()));

        stubs.enqueueResponses(response);
        stubs.stubDiscovery(stubs.k8sInstance(service, instanceId));

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.get(InstanceId.of(instanceId)))
                .get()
                .extracting(Instance::detectedFeatures)
                .isEqualTo(new DetectedFeatures(EnumSet.noneOf(InstanceFeature.class)));
    }
}
