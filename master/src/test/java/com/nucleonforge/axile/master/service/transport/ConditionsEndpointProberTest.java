/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.transport;

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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link BeansEndpointProber}.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class ConditionsEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ConditionsEndpointProber conditionsEndpointProber;

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
          "positiveConditions": [
            {
              "target": "EndpointAutoConfiguration#propertiesEndpointAccessResolver",
              "matches": [
                {
                  "condition": "OnBeanCondition",
                  "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"
                }
              ]
            },
            {
              "target": "EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor",
              "matches": [
                {
                  "condition": "OnBeanCondition",
                  "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"
                }
              ]
            }
          ],
          "negativeConditions": [
            {
              "target": "WebFluxEndpointManagementContextConfiguration",
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
              "target": "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration",
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

                if (path.equals("/" + activeInstanceId + "/actuator/conditions")) {
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
    void shouldReturnConditionsFeed() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ConditionsFeed feed = conditionsEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();

        assertThat(feed.positiveConditions()).hasSize(2);

        var positive1 = feed.positiveConditions().get(0);
        assertThat(positive1.target()).isEqualTo("EndpointAutoConfiguration#propertiesEndpointAccessResolver");
        assertThat(positive1.matches()).hasSize(1);
        assertThat(positive1.matches().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive1.matches().get(0).message()).contains("@ConditionalOnMissingBean");

        var positive2 = feed.positiveConditions().get(1);
        assertThat(positive2.target()).isEqualTo("EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor");
        assertThat(positive2.matches()).hasSize(1);
        assertThat(positive2.matches().get(0).condition()).isEqualTo("OnBeanCondition");
        assertThat(positive2.matches().get(0).message()).contains("CachingOperationInvokerAdvisor");

        assertThat(feed.negativeConditions()).hasSize(2);

        var negative1 = feed.negativeConditions().get(0);
        assertThat(negative1.target()).isEqualTo("WebFluxEndpointManagementContextConfiguration");
        assertThat(negative1.notMatched()).hasSize(1);
        assertThat(negative1.notMatched().get(0).condition()).isEqualTo("OnWebApplicationCondition");
        assertThat(negative1.notMatched().get(0).message()).isEqualTo("not a reactive web application");
        assertThat(negative1.matched()).hasSize(1);
        assertThat(negative1.matched().get(0).condition()).isEqualTo("OnClassCondition");
        assertThat(negative1.matched().get(0).message()).contains("DispatcherHandler");

        var negative2 = feed.negativeConditions().get(1);
        assertThat(negative2.target())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration");
        assertThat(negative2.notMatched()).hasSize(1);
        assertThat(negative2.notMatched().get(0).condition())
                .isEqualTo("GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition");
        assertThat(negative2.notMatched().get(0).message()).contains("AnyNestedCondition");
        assertThat(negative2.matched()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> conditionsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> conditionsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
