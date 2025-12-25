/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.utils.TestObjectFactory;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link InfoApi}.
 *
 * @since 28.08.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@TestPropertySource(properties = "axile.master.auth.type=none")
public class InfoApiTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

    // language=json
    private static final String EXPECTED_INFO_JSON =
            """
            {
              "git" : {
                "branch" : "main",
                "commit" : {
                  "id" : "df027cf",
                  "time" : "2025-08-21T09:11:35Z"
                }
              },
              "build" : {
                "artifact" : "application",
                "version" : "1.0.3",
                "group" : "com.example"
              },
              "os" : {
                "name" : "Linux",
                "version" : "6.11.0-1018-azure",
                "arch" : "amd64"
              },
              "process" : {
                "pid" : 91003,
                "parentPid" : 89732,
                "owner" : "runner",
                "memory" : {
                  "heap" : {
                    "max" : 1610612736,
                    "used" : 97295984,
                    "committed" : 143654912,
                    "init" : 262144000
                  },
                  "nonHeap" : {
                    "max" : -1,
                    "used" : 92845808,
                    "committed" : 95485952,
                    "init" : 7667712
                  },
                  "garbageCollectors" : [ {
                    "name" : "G1 Young Generation",
                    "collectionCount" : 15
                  }, {
                    "name" : "G1 Old Generation",
                    "collectionCount" : 0
                  } ]
                },
                "cpus" : 4
              },
              "java" : {
                "version" : "17.0.16",
                "vendor" : {
                  "name" : "BellSoft"
                },
                "runtime" : {
                  "name" : "OpenJDK Runtime Environment",
                  "version" : "17.0.16+12-LTS"
                },
                "jvm" : {
                  "name" : "OpenJDK 64-Bit Server VM",
                  "vendor" : "BellSoft",
                  "version" : "17.0.16+12-LTS"
                }
              },
              "ssl" : {
                "bundles" : [ {
                  "name" : "test-0",
                  "certificateChains" : [ {
                    "alias" : "spring-boot",
                    "certificates" : [ {
                      "version" : "V3",
                      "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "validity" : {
                        "status" : "VALID"
                      },
                      "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "serialNumber" : "eb6114a6ae39ce6c",
                      "signatureAlgorithmName" : "SHA256withRSA",
                      "validityStarts" : "2023-05-05T11:26:57Z",
                      "validityEnds" : "2123-04-11T11:26:57Z"
                    } ]
                  }, {
                    "alias" : "test-alias",
                    "certificates" : [ {
                      "version" : "V3",
                      "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "validity" : {
                        "status" : "VALID"
                      },
                      "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "serialNumber" : "14ca9ba6abe2a70d",
                      "signatureAlgorithmName" : "SHA256withRSA",
                      "validityStarts" : "2023-05-05T11:26:58Z",
                      "validityEnds" : "2123-04-11T11:26:58Z"
                    } ]
                  }, {
                    "alias" : "spring-boot-cert",
                    "certificates" : [ ]
                  }, {
                    "alias" : "test-alias-cert",
                    "certificates" : [ ]
                  } ]
                } ]
              }
            }
            """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

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
        // language=json
        String jsonResponse =
                """
            {
              "git" : {
                "branch" : "main",
                "commit" : {
                  "id" : "df027cf",
                  "time" : "2025-08-21T09:11:35Z"
                }
              },
              "build" : {
                "artifact" : "application",
                "version" : "1.0.3",
                "group" : "com.example"
              },
              "os" : {
                "name" : "Linux",
                "version" : "6.11.0-1018-azure",
                "arch" : "amd64"
              },
              "process" : {
                "pid" : 91003,
                "parentPid" : 89732,
                "owner" : "runner",
                "memory" : {
                  "heap" : {
                    "max" : 1610612736,
                    "used" : 97295984,
                    "committed" : 143654912,
                    "init" : 262144000
                  },
                  "nonHeap" : {
                    "max" : -1,
                    "used" : 92845808,
                    "committed" : 95485952,
                    "init" : 7667712
                  },
                  "garbageCollectors" : [ {
                    "name" : "G1 Young Generation",
                    "collectionCount" : 15
                  }, {
                    "name" : "G1 Old Generation",
                    "collectionCount" : 0
                  } ]
                },
                "cpus" : 4
              },
              "java" : {
                "version" : "17.0.16",
                "vendor" : {
                  "name" : "BellSoft"
                },
                "runtime" : {
                  "name" : "OpenJDK Runtime Environment",
                  "version" : "17.0.16+12-LTS"
                },
                "jvm" : {
                  "name" : "OpenJDK 64-Bit Server VM",
                  "vendor" : "BellSoft",
                  "version" : "17.0.16+12-LTS"
                }
              },
              "ssl" : {
                "bundles" : [ {
                  "name" : "test-0",
                  "certificateChains" : [ {
                    "alias" : "spring-boot",
                    "certificates" : [ {
                      "version" : "V3",
                      "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "validity" : {
                        "status" : "VALID"
                      },
                      "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "serialNumber" : "eb6114a6ae39ce6c",
                      "signatureAlgorithmName" : "SHA256withRSA",
                      "validityStarts" : "2023-05-05T11:26:57Z",
                      "validityEnds" : "2123-04-11T11:26:57Z"
                    } ]
                  }, {
                    "alias" : "test-alias",
                    "certificates" : [ {
                      "version" : "V3",
                      "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "validity" : {
                        "status" : "VALID"
                      },
                      "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                      "serialNumber" : "14ca9ba6abe2a70d",
                      "signatureAlgorithmName" : "SHA256withRSA",
                      "validityStarts" : "2023-05-05T11:26:58Z",
                      "validityEnds" : "2123-04-11T11:26:58Z"
                    } ]
                  }, {
                    "alias" : "spring-boot-cert",
                    "certificates" : [ ]
                  }, {
                    "alias" : "test-alias-cert",
                    "certificates" : [ ]
                  } ]
                } ]
              }
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/info")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnJSONServiceInfo() {
        // when.
        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/info/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_INFO_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/info/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-info-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/info/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
