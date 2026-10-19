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
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import io.fabric8.kubernetes.client.KubernetesClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
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
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.features.DetectedFeatures;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesServiceInstance;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
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

    private URI uri;

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
        uri = URI.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
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

        // language=json
        String response = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "groupId" : "org.springframework.samples",
              "artifactId" : "%s",
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
            }
        """;

        enqueueMetadata(response.formatted("app1"), response.formatted("app2"));
        stubDiscovery(k8sInstance(service1, instance1Id), k8sInstance(service2, instance2Id));

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

        // language=json
        String failedExecutions = """
            [ {
              "taskId" : "com.example.OwnerJob#run()",
              "startedAt" : "2026-09-27T10:00:00.123Z",
              "durationMillis" : 1500,
              "success" : false,
              "errorType" : "NullPointerException",
              "errorMessage" : "boom"
            } ]""";

        // language=json
        String successfulExecutions = """
            [ {
              "taskId" : "com.example.OwnerArchivistTask#archive()",
              "startedAt" : "2026-09-27T10:05:00.500Z",
              "durationMillis" : 42,
              "success" : true
            } ]""";

        // language=json
        String metadata = """
            {
              "version" : "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "groupId" : "%s",
              "artifactId" : "%s",
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
                },
                "scheduledTaskExecutions" : %s
              }
            }
            """;

        enqueueMetadata(
                metadata.formatted(groupId, artifactId, failedExecutions),
                metadata.formatted(groupId, artifactId, successfulExecutions));
        stubDiscovery(
                k8sInstance(serviceWithFailedExecution, failedInstanceId),
                k8sInstance(serviceWithSuccessfulExecution, successfulInstanceId));

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
                        ScheduledTaskExecutionResult::errorType,
                        ScheduledTaskExecutionResult::errorMessage)
                .containsExactlyInAnyOrder(
                        tuple(
                                failedInstanceId,
                                failedTaskId,
                                Instant.parse("2026-09-27T10:00:00.123Z"),
                                1500L,
                                false,
                                "NullPointerException",
                                "boom"),
                        tuple(
                                successfulInstanceId,
                                successfulTaskId,
                                Instant.parse("2026-09-27T10:05:00.500Z"),
                                42L,
                                true,
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

        // language=json
        String firstResponse = """
            {
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
            }
            """;

        // language=json
        String secondResponse = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "groupId" : "org.springframework.samples",
              "artifactId" : "petclinic",
              "commitShortSha" : "910230",
              "jdkVendor" : "BellSoft",
              "gcInUse" : "G1",
              "softwareVersions" : {
                "springBoot" : "3.5.2",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "DOWN",
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
            }
            """;

        enqueueMetadata(firstResponse, secondResponse);
        stubDiscovery(k8sInstance(service, instanceId));
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
    void shouldRefreshKnownInstanceViaMetadataV2Endpoint() throws InterruptedException {
        String service = "service-with-metadata-v2";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String metadataResponse = """
            {
              "version": "1.3.0",
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
            }
            """;

        // language=json
        String metadataV2Response = """
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

        mockWebServer.enqueue(
                new MockResponse().setBody(metadataResponse).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(new MockResponse()
                .setBody(metadataV2Response)
                .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        ServiceInstance k8sInstance = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), service)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(service));
        Mockito.when(discoveryClient.getInstances(service)).thenReturn(List.of(k8sInstance));
        subject.performDiscovery();

        // when.
        subject.performDiscovery();

        // then.
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-metadata");
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-metadata/v2");

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
    void shouldDeregisterK8sInstanceWhenNoLongerInDiscovery() {
        String serviceId = "test-service";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "groupId" : "org.springframework.samples",
              "artifactId" : "petclinic",
              "commitShortSha" : "910230",
              "jdkVendor" : "BellSoft",
              "gcInUse" : "G1",
              "softwareVersions" : {
                "springBoot" : "3.5.2",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "DOWN",
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
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/actuator/axelix-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ServiceInstance k8sServiceInstance = k8sInstance(serviceId, instanceId);

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

        // language=json
        String response = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "groupId" : "org.springframework.samples",
              "artifactId" : "petclinic",
              "commitShortSha" : "910230",
              "jdkVendor" : "BellSoft",
              "gcInUse" : "G1",
              "softwareVersions" : {
                "springBoot" : "3.5.2",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "DOWN",
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
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/actuator/axelix-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ServiceInstance k8sServiceInstance = k8sInstance(serviceId, instanceId);

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

        // language=json
        String response = """
            {
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
                  "transactions" : [ ]
                }
              }
            }
        """;

        enqueueMetadata(response);
        stubDiscovery(k8sInstance(service, instanceId));

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

        // language=json
        String response = """
            {
              "version": "1.3.0",
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
              },
              "detectedFeatures" : [ ]
            }
            """;

        enqueueMetadata(response);
        stubDiscovery(k8sInstance(service, instanceId));

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.get(InstanceId.of(instanceId)))
                .get()
                .extracting(Instance::detectedFeatures)
                .isEqualTo(new DetectedFeatures(EnumSet.noneOf(InstanceFeature.class)));
    }

    private void enqueueMetadata(String... responses) {
        for (String response : responses) {
            mockWebServer.enqueue(
                    new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        }
    }

    private ServiceInstance k8sInstance(String serviceId, String instanceId) {
        return Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), serviceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();
    }

    private void stubDiscovery(ServiceInstance... instances) {
        List<String> serviceIds =
                Arrays.stream(instances).map(ServiceInstance::getServiceId).toList();
        Mockito.when(discoveryClient.getServices()).thenReturn(serviceIds);

        for (ServiceInstance instance : instances) {
            Mockito.when(discoveryClient.getInstances(instance.getServiceId())).thenReturn(List.of(instance));
        }
    }
}
