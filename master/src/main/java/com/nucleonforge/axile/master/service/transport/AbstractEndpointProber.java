package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The common implementation of the {@link EndpointProber}.
 *
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
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public @NonNull O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {

        ActuatorEndpoint endpoint = supports();

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    buildHttpRequest(endpoint, instanceId, httpPayload), HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                byte[] responseBody = response.body();
                return messageDeserializationStrategy.deserialize(responseBody);
            } else {
                throw new EndpointInvocationException(unexpectedStatusCode(instanceId, endpoint, statusCode));
            }

        } catch (IOException | InterruptedException e) {
            throw new EndpointInvocationException(e);
        }
    }

    private HttpRequest buildHttpRequest(ActuatorEndpoint endpoint, InstanceId instanceId, HttpPayload httpPayload) {
        Instance instance =
                instanceRegistry.get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));

        BodyPublisher bodyPublisher =
                httpPayload.hasBody() ? BodyPublishers.ofByteArray(httpPayload.requestBody()) : BodyPublishers.noBody();

        HttpRequest.Builder builder =
                HttpRequest.newBuilder().method(endpoint.httpMethod().name(), bodyPublisher);

        if (httpPayload.hasHeaders()) {
            for (var header : httpPayload.headers()) {
                builder.header(header.name(), header.valueAsString());
            }
        }

        return builder.uri(buildUrl(endpoint, httpPayload, instance)).build();
    }

    private static URI buildUrl(ActuatorEndpoint endpoint, HttpPayload httpPayload, Instance instance) {
        return URI.create(instance.getActuatorUrl()
                + endpoint.path().expand(httpPayload.pathVariableValues(), httpPayload.queryParameters()));
    }

    private static String unexpectedStatusCode(InstanceId instanceId, ActuatorEndpoint endpoint, int statusCode) {
        return "Endpoint '%s' when invoked on instance '%s' did not respond with 2xx response, but with %d"
                .formatted(endpoint.path(), instanceId.instanceId(), statusCode);
    }
}
