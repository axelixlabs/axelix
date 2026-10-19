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
package com.axelixlabs.axelix.master.service.transport;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.ThrowableAssert;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.auth.StaticTestSecurityContextExecutor;

import static com.axelixlabs.axelix.common.domain.ActuatorEndpoint.of;
import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for {@link DefaultEndpointInvoker}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(DefaultEndpointInvokerTest.DefaultEndpointInvokerTestConfiguration.class)
public class DefaultEndpointInvokerTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String FIRST_INSTANCE_ID = "first-instance";
    private static final String SECOND_INSTANCE_ID = "second-instance";
    private static final String FAILING_INSTANCE_ID = "failing-instance";

    public static final ActuatorEndpoint METHOD_INVOKE = of("/axelix-test/invoke", HttpMethod.GET);
    public static final ActuatorEndpoint METHOD_INVOKE_BAD_REQUEST =
            of("/axelix-test/invoke/bad-request", HttpMethod.GET);
    public static final ActuatorEndpoint METHOD_INVOKE_NO_VALUE = of("/axelix-test/invoke-no-value", HttpMethod.POST);
    public static final ActuatorEndpoint METHOD_INVOKE_NO_VALUE_BAD_REQUEST =
            of("/axelix-test/invoke-no-value/bad-request", HttpMethod.POST);

    private static MockWebServer mockWebServer;

    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private EndpointInvoker endpointInvoker;

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
        requests.clear();
        // language=json
        String jsonResponse = """
        {
          "cacheManagers" : [
            {
              "name": "anotherCacheManager",
              "caches": [
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "hitsCount" : 15,
                  "missesCount" : 2,
                  "estimatedEntrySize": 10,
                  "enabled": false
                }
              ]
            }
          ]
        }
        """;
        mockWebServer.setDispatcher(new Dispatcher() {

            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                requests.add(request);
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + FAILING_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value")) {
                    return new MockResponse().setResponseCode(500);
                } else if (path.endsWith("/actuator/axelix-test/invoke")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.endsWith("/actuator/axelix-test/invoke/bad-request")
                        || path.endsWith("/actuator/axelix-test/invoke-no-value/bad-request")) {
                    return new MockResponse().setResponseCode(400);
                } else if (path.endsWith("/actuator/axelix-test/invoke-no-value")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(TestInstanceFactory.create(
                activeInstanceId, mockWebServer.url("/actuator").toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void invoke_shouldReturnObject() {
        JsonNode result =
                endpointInvoker.invoke(InstanceId.of(activeInstanceId), METHOD_INVOKE, NoHttpPayload.INSTANCE);

        assertThat(result).isNotNull();
        assertThat(result.isObject()).isTrue();
    }

    @Test
    void invoke_shouldReturnEndpointInvocationException_OnUnavailableInstance() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(TestInstanceFactory.create(instanceId));

        assertThatThrownBy(
                        () -> endpointInvoker.invoke(InstanceId.of(instanceId), METHOD_INVOKE, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void invoke_shouldReturnEndpointInvocationException_OnUnknownActuatorEndpoint() {
        ThrowableAssert.ThrowingCallable callable = () -> endpointInvoker.invoke(
                InstanceId.of(activeInstanceId), ActuatorEndpoint.of("other", HttpMethod.POST), NoHttpPayload.INSTANCE);

        assertThatThrownBy(callable).isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void invoke_shouldReturnInstanceNotFoundException() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(
                        () -> endpointInvoker.invoke(InstanceId.of(instanceId), METHOD_INVOKE, NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    void invoke_shouldReturnBadRequestException_OnNotValidRequest() {
        assertThatThrownBy(() -> endpointInvoker.invoke(
                        InstanceId.of(activeInstanceId), METHOD_INVOKE_BAD_REQUEST, NoHttpPayload.INSTANCE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void invoke_shouldReturnClassCastException() {
        assertThrows(ClassCastException.class, () -> {
            String result =
                    endpointInvoker.invoke(InstanceId.of(activeInstanceId), METHOD_INVOKE, NoHttpPayload.INSTANCE);
        });
    }

    @Test
    void invokeNoValue_NoException() {
        assertThatNoException()
                .isThrownBy(() -> endpointInvoker.invokeNoValue(
                        InstanceId.of(activeInstanceId), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE));
    }

    @Test
    void invokeNoValue_shouldReturnEndpointInvocationException() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(TestInstanceFactory.create(instanceId));

        assertThatThrownBy(() -> endpointInvoker.invoke(
                        InstanceId.of(instanceId), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void invokeNoValue_shouldReturnInstanceNotFoundException() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> endpointInvoker.invoke(
                        InstanceId.of(instanceId), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    void invokeNoValue_shouldReturnBadRequestException_OnNotValidRequest() {
        assertThatThrownBy(() -> endpointInvoker.invokeNoValue(
                        InstanceId.of(activeInstanceId), METHOD_INVOKE_NO_VALUE_BAD_REQUEST, NoHttpPayload.INSTANCE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void invokeNoValueForInstances_shouldInvokeEndpointNoValueForAllInstances() {
        // given.
        Instance firstInstance = TestInstanceFactory.create(
                FIRST_INSTANCE_ID,
                mockWebServer.url("/" + FIRST_INSTANCE_ID + "/actuator").toString());
        Instance secondInstance = TestInstanceFactory.create(
                SECOND_INSTANCE_ID,
                mockWebServer.url("/" + SECOND_INSTANCE_ID + "/actuator").toString());

        // when.
        assertThatNoException()
                .isThrownBy(() -> endpointInvoker.invokeNoValueForInstances(
                        List.of(firstInstance, secondInstance), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE));

        // then.
        assertThat(requests)
                .extracting(request -> request.getMethod() + " " + request.getPath())
                .containsExactlyInAnyOrder(
                        "POST /" + FIRST_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value",
                        "POST /" + SECOND_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value");
    }

    @Test
    void invokeNoValueForInstances_shouldThrowPartiallyUpdatedException_WhenSomeInstancesFail() {
        // given.
        Instance firstInstance = TestInstanceFactory.create(
                FIRST_INSTANCE_ID,
                mockWebServer.url("/" + FIRST_INSTANCE_ID + "/actuator").toString());
        Instance failingInstance = TestInstanceFactory.create(
                FAILING_INSTANCE_ID,
                mockWebServer.url("/" + FAILING_INSTANCE_ID + "/actuator").toString());

        // when.
        ThrowableAssert.ThrowingCallable callable = () -> endpointInvoker.invokeNoValueForInstances(
                List.of(firstInstance, failingInstance), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE);

        // then.
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(
                        PartiallyUpdatedException.class,
                        e -> assertThat(e.failedInstanceIds()).containsExactly(FAILING_INSTANCE_ID));
        assertThat(requests)
                .extracting(request -> request.getMethod() + " " + request.getPath())
                .containsExactlyInAnyOrder(
                        "POST /" + FIRST_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value",
                        "POST /" + FAILING_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value");
    }

    @Test
    void invokeNoValueForInstances_shouldThrowPartiallyUpdatedException_WhenAllInstancesFail() {
        // given.
        Instance firstInstance = TestInstanceFactory.create(
                FIRST_INSTANCE_ID,
                mockWebServer.url("/" + FIRST_INSTANCE_ID + "/actuator").toString());
        Instance secondInstance = TestInstanceFactory.create(
                SECOND_INSTANCE_ID,
                mockWebServer.url("/" + SECOND_INSTANCE_ID + "/actuator").toString());

        // when.
        ThrowableAssert.ThrowingCallable callable = () -> endpointInvoker.invokeNoValueForInstances(
                List.of(firstInstance, secondInstance), METHOD_INVOKE_NO_VALUE_BAD_REQUEST, NoHttpPayload.INSTANCE);

        // then.
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(
                        PartiallyUpdatedException.class,
                        e -> assertThat(e.failedInstanceIds())
                                .containsExactlyInAnyOrder(FIRST_INSTANCE_ID, SECOND_INSTANCE_ID));
        assertThat(requests)
                .extracting(request -> request.getMethod() + " " + request.getPath())
                .containsExactlyInAnyOrder(
                        "POST /" + FIRST_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value/bad-request",
                        "POST /" + SECOND_INSTANCE_ID + "/actuator/axelix-test/invoke-no-value/bad-request");
    }

    @Test
    void invokeNoValueForInstances_shouldThrowEndpointInvocationException_OnUnknownActuatorEndpoint() {
        // given.
        Instance firstInstance = TestInstanceFactory.create(
                FIRST_INSTANCE_ID,
                mockWebServer.url("/" + FIRST_INSTANCE_ID + "/actuator").toString());

        // when.
        ThrowableAssert.ThrowingCallable callable = () -> endpointInvoker.invokeNoValueForInstances(
                List.of(firstInstance), of("/unknown", HttpMethod.POST), NoHttpPayload.INSTANCE);

        // then.
        assertThatThrownBy(callable).isInstanceOf(EndpointInvocationException.class);
        assertThat(requests).isEmpty();
    }

    @Test
    void invokeNoValueForInstances_shouldUseLoadedInstanceUrls() {
        // given.
        Instance secondInstance = TestInstanceFactory.create(
                SECOND_INSTANCE_ID, mockWebServer.url("/second-url/actuator").toString());
        Instance firstInstance = TestInstanceFactory.create(
                FIRST_INSTANCE_ID, mockWebServer.url("/first-url/actuator").toString());

        // when.
        endpointInvoker.invokeNoValueForInstances(
                List.of(firstInstance, secondInstance), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE);

        // then.
        assertThat(requests)
                .extracting(request -> request.getMethod() + " " + request.getPath())
                .containsExactlyInAnyOrder(
                        "POST /first-url/actuator/axelix-test/invoke-no-value",
                        "POST /second-url/actuator/axelix-test/invoke-no-value");
    }

    @Test
    void invokeNoValueForInstances_shouldInvokeUsingLoadedActuatorUrl() {
        // given.
        Instance instance = TestInstanceFactory.create(
                activeInstanceId, mockWebServer.url("/actuator").toString());

        // when.
        endpointInvoker.invokeNoValueForInstances(List.of(instance), METHOD_INVOKE_NO_VALUE, NoHttpPayload.INSTANCE);

        // then.
        assertThat(requests)
                .extracting(request -> request.getMethod() + " " + request.getPath())
                .containsExactly("POST /actuator/axelix-test/invoke-no-value");
    }

    @TestConfiguration
    static class DefaultEndpointInvokerTestConfiguration {

        @Bean
        @Primary
        public SecurityContextExecutor testSecurityContextExecutor() {
            return new StaticTestSecurityContextExecutor();
        }

        @Bean
        public DiscardingAbstractEndpointProber invokeNoValueMethodEndpointProber(
                InstanceRegistry instanceRegistry,
                SecurityContextExecutor securityContextExecutor,
                HttpClient httpClient) {
            return new DiscardingAbstractEndpointProber(
                    instanceRegistry, METHOD_INVOKE_NO_VALUE, securityContextExecutor, httpClient);
        }

        @Bean
        public DiscardingAbstractEndpointProber invokeNoValueMethodWithBadRequestEndpointProber(
                InstanceRegistry instanceRegistry,
                SecurityContextExecutor securityContextExecutor,
                HttpClient httpClient) {
            return new DiscardingAbstractEndpointProber(
                    instanceRegistry, METHOD_INVOKE_NO_VALUE_BAD_REQUEST, securityContextExecutor, httpClient);
        }

        @Bean
        public DefaultEndpointProber<JsonNode> invokeMethodEndpointProber(
                InstanceRegistry instanceRegistry,
                TestJacksonMessageDeserializationStrategy deserializationStrategy,
                SecurityContextExecutor securityContextExecutor,
                HttpClient httpClient) {
            return new DefaultEndpointProber<>(
                    instanceRegistry, deserializationStrategy, securityContextExecutor, METHOD_INVOKE, httpClient);
        }

        @Bean
        public DefaultEndpointProber<JsonNode> invokeMethodWithBadRequestEndpointProber(
                InstanceRegistry instanceRegistry,
                TestJacksonMessageDeserializationStrategy deserializationStrategy,
                SecurityContextExecutor securityContextExecutor,
                HttpClient httpClient) {
            return new DefaultEndpointProber<>(
                    instanceRegistry,
                    deserializationStrategy,
                    securityContextExecutor,
                    METHOD_INVOKE_BAD_REQUEST,
                    httpClient);
        }

        @Component
        static class TestJacksonMessageDeserializationStrategy extends JacksonMessageDeserializationStrategy<JsonNode> {

            public TestJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
                super(objectMapper);
            }

            @Override
            public @NonNull Class<JsonNode> supported() {
                return JsonNode.class;
            }
        }
    }
}
