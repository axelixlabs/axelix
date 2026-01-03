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
package com.nucleonforge.axelix.master.service.transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.serde.DeserializationException;
import com.nucleonforge.axelix.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

/**
 * The common implementation of the {@link EndpointProber}.
 *
 * @param <O> the type of the response body (output).
 * @author Mikhail Polivakha
 */
public abstract class AbstractEndpointProber<O> implements EndpointProber<O> {

    private final InstanceRegistry instanceRegistry;
    private final MessageDeserializationStrategy<O> messageDeserializationStrategy;
    private final HttpClient httpClient;

    protected AbstractEndpointProber(
            InstanceRegistry instanceRegistry, MessageDeserializationStrategy<O> messageDeserializationStrategy) {
        this.instanceRegistry = instanceRegistry;
        this.messageDeserializationStrategy = messageDeserializationStrategy;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @Override
    public @NonNull O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {
        Instance instance =
                instanceRegistry.get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));

        HttpRequest request = buildHttpRequest(supports(), httpPayload, instance.actuatorUrl());

        return invokeInternal(instanceId.instanceId(), request);
    }

    @Override
    public @NonNull O invoke(@NonNull String baseUrl, HttpPayload httpPayload) throws EndpointInvocationException {
        HttpRequest request = buildHttpRequest(supports(), httpPayload, baseUrl);
        return invokeInternal(baseUrl, request);
    }

    private O invokeInternal(String instanceIdentity, HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return messageDeserializationStrategy.deserialize(response.body());
            } else {
                throw new EndpointInvocationException("Endpoint '%s' on instance identified by '%s' responded with %d"
                        .formatted(supports(), instanceIdentity, statusCode));
            }

            // TODO:
            //  write integration test to check that correct exception is thrown from AbstractEndpointProber
            //  when deserializationStrategy fails
        } catch (IOException | InterruptedException | DeserializationException e) {
            throw new EndpointInvocationException(e);
        }
    }

    private HttpRequest buildHttpRequest(ActuatorEndpoint endpoint, HttpPayload httpPayload, String baseOrActuatorUrl) {

        String url = baseOrActuatorUrl
                + endpoint.path().expand(httpPayload.pathVariableValues(), httpPayload.queryParameters());

        BodyPublisher bodyPublisher =
                httpPayload.hasBody() ? BodyPublishers.ofByteArray(httpPayload.requestBody()) : BodyPublishers.noBody();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .method(endpoint.httpMethod().name(), bodyPublisher)
                .uri(URI.create(url));

        if (httpPayload.hasHeaders()) {
            for (var header : httpPayload.headers()) {
                builder.header(header.name(), header.valueAsString());
            }
        }

        return builder.build();
    }
}
