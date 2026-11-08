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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.jspecify.annotations.NonNull;

import org.springframework.http.HttpHeaders;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.auth.StarterAuthenticationTokenProvider;
import com.axelixlabs.axelix.master.service.serde.DeserializationException;
import com.axelixlabs.axelix.master.service.serde.MessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

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
    private final StarterAuthenticationTokenProvider tokenProvider;

    protected AbstractEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<O> messageDeserializationStrategy,
            StarterAuthenticationTokenProvider tokenProvider,
            HttpClient httpClient) {
        this.instanceRegistry = instanceRegistry;
        this.messageDeserializationStrategy = messageDeserializationStrategy;
        this.tokenProvider = tokenProvider;
        this.httpClient = httpClient;
    }

    @Override
    public @NonNull O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException {
        Instance instance = instanceRegistry.getOrThrow(instanceId);

        HttpRequest request = buildHttpRequest(
                supports(), httpPayload, instance.actuatorUrl(), tokenProvider.tokenFor(instance.starterVersion()));

        return invokeInternal(instanceId.instanceId(), request);
    }

    @Override
    public @NonNull O invoke(@NonNull String baseUrl, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException {
        HttpRequest request = buildHttpRequest(supports(), httpPayload, baseUrl, tokenProvider.discoveryToken());
        return invokeInternal(baseUrl, request);
    }

    private O invokeInternal(String instanceIdentity, HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return messageDeserializationStrategy.deserialize(response.body());
            } else if (statusCode == 400) {
                throw new BadRequestException("Endpoint '%s' on instance identified by '%s' responded with %d"
                        .formatted(supports(), instanceIdentity, statusCode));
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

    private HttpRequest buildHttpRequest(
            ActuatorEndpoint endpoint, HttpPayload httpPayload, String baseOrActuatorUrl, String token) {

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

        builder.setHeader(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + token);

        return builder.build();
    }
}
