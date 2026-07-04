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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConditionsApi}.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConditionsApiTest {

    // language=json
    private static final String EXPECTED_CONDITIONS_JSON = """
    {
      "positiveMatches": [
        {
          "className" : "EndpointAutoConfiguration",
          "methodName" : "propertiesEndpointAccessResolver",
          "matched": [
            {
              "condition": "OnBeanCondition",
              "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"
            }
          ]
        },
        {
          "className" : "EndpointAutoConfiguration",
          "methodName" : "endpointCachingOperationInvokerAdvisor",
          "matched": [
            {
              "condition": "OnBeanCondition",
               "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"
            }
          ]
        }
      ],
      "negativeMatches": [
        {
          "className": "WebFluxEndpointManagementContextConfiguration",
          "methodName": null,
          "notMatched": [
            {
              "condition": "OnWebApplicationCondition",
              "message": "not a reactive web application"
            }
          ],
          "matched": [
            {
              "condition": "OnClassCondition",
              "message": "@ConditionalOnClass found required classes 'org.springframework.web.reactive.DispatcherHandler', 'org.springframework.http.server.reactive.HttpHandler'"
            }
          ]
        },
        {
          "className": "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
          "methodName": null,
          "notMatched": [
            {
              "condition": "GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition",
              "message": "AnyNestedCondition 0 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions"
            }
          ],
          "matched": []
        }
      ]
    }
    """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

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
        // language=json
        String jsonResponse = """
        {
          "positiveMatches": [
            {
              "className" : "EndpointAutoConfiguration",
              "methodName" : "propertiesEndpointAccessResolver",
              "matched": [
                {
                  "condition": "OnBeanCondition",
                  "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"
                }
              ]
            },
            {
              "className" : "EndpointAutoConfiguration",
              "methodName" : "endpointCachingOperationInvokerAdvisor",
              "matched": [
                {
                  "condition": "OnBeanCondition",
                   "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"
                }
              ]
            }
          ],
          "negativeMatches": [
            {
              "className": "WebFluxEndpointManagementContextConfiguration",
              "methodName": null,
              "notMatched": [
                {
                  "condition": "OnWebApplicationCondition",
                  "message": "not a reactive web application"
                }
              ],
              "matched": [
                {
                  "condition": "OnClassCondition",
                  "message": "@ConditionalOnClass found required classes 'org.springframework.web.reactive.DispatcherHandler', 'org.springframework.http.server.reactive.HttpHandler'"
                }
              ]
            },
            {
              "className": "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
              "methodName": null,
              "notMatched": [
                {
                  "condition": "GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition",
                  "message": "AnyNestedCondition 0 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions"
                }
              ],
              "matched": []
            }
          ]
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-conditions")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.reload(
                TestInstanceFactory.create(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONConditionsFeed() {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/conditions/feed/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_CONDITIONS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();
        registry.reload(TestInstanceFactory.create(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/conditions/feed/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/conditions/feed/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(
            method = HttpMethod.GET,
            path = "/api/external/conditions/feed/00000000-0000-0000-0000-000000000001")
    void negativeAuthTests() {}
}
