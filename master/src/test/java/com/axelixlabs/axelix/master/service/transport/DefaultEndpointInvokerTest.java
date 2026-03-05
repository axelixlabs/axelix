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
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.http.HttpUrl;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;

import static com.axelixlabs.axelix.master.domain.ActuatorEndpoint.of;
import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for {@link DefaultEndpointInvoker}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
@Import(DefaultEndpointInvokerTest.DefaultEndpointInvokerTestConfiguration.class)
public class DefaultEndpointInvokerTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    public static final ActuatorEndpoint METHOD_INVOKE = of("/axelix-test/invoke", HttpMethod.GET);
    public static final ActuatorEndpoint METHOD_INVOKE_BAD_REQUEST =
            of("/axelix-test/invoke/bad-request", HttpMethod.GET);
    public static final ActuatorEndpoint METHOD_INVOKE_NO_VALUE = of("/axelix-test/invoke-no-value", HttpMethod.POST);
    public static final ActuatorEndpoint METHOD_INVOKE_NO_VALUE_BAD_REQUEST =
            of("/axelix-test/invoke-no-value/bad-request", HttpMethod.POST);

    private static MockWebServer mockWebServer;

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
                String path = request.getPath();
                assert path != null;

                if (path.equals("/actuator/axelix-test/invoke")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/actuator/axelix-test/invoke/bad-request")) {
                    return new MockResponse().setResponseCode(400);
                } else if (path.equals("/actuator/axelix-test/invoke-no-value")) {
                    return new MockResponse();
                } else if (path.equals("/actuator/axelix-test/invoke-no-value/bad-request")) {
                    return new MockResponse().setResponseCode(400);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(TestObjectFactory.createInstance(
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
        registry.register(createInstance(instanceId));

        assertThatThrownBy(
                        () -> endpointInvoker.invoke(InstanceId.of(instanceId), METHOD_INVOKE, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void invoke_shouldReturnEndpointInvocationException_OnUnknownActuatorEndpoint() {
        ThrowableAssert.ThrowingCallable callable = () -> endpointInvoker.invoke(
                InstanceId.of(activeInstanceId),
                new ActuatorEndpoint(new HttpUrl("other"), HttpMethod.POST),
                NoHttpPayload.INSTANCE);

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
        registry.register(createInstance(instanceId));

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

    @TestConfiguration
    static class DefaultEndpointInvokerTestConfiguration {

        @Bean
        public DiscardingAbstractEndpointProber invokeNoValueMethodEndpointProber(InstanceRegistry instanceRegistry) {
            return new DiscardingAbstractEndpointProber(instanceRegistry, METHOD_INVOKE_NO_VALUE);
        }

        @Bean
        public DiscardingAbstractEndpointProber invokeNoValueMethodWithBadRequestEndpointProber(
                InstanceRegistry instanceRegistry) {
            return new DiscardingAbstractEndpointProber(instanceRegistry, METHOD_INVOKE_NO_VALUE_BAD_REQUEST);
        }

        @Bean
        public DefaultEndpointProber<JsonNode> invokeMethodEndpointProber(
                InstanceRegistry instanceRegistry, TestJacksonMessageDeserializationStrategy deserializationStrategy) {
            return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, METHOD_INVOKE);
        }

        @Bean
        public DefaultEndpointProber<JsonNode> invokeMethodWithBadRequestEndpointProber(
                InstanceRegistry instanceRegistry, TestJacksonMessageDeserializationStrategy deserializationStrategy) {
            return new DefaultEndpointProber<>(instanceRegistry, deserializationStrategy, METHOD_INVOKE_BAD_REQUEST);
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
