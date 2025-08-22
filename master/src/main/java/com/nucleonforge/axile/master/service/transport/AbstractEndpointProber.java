package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;
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
    public @NonNull O invoke(@NonNull InstanceId instanceId)
            throws EndpointInvocationException, InstanceNotFoundException {
        Instance instance =
                instanceRegistry.get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));
        ActuatorEndpoint endpoint = supports();
        String targetUrl = instance.urlForEndpoint(endpoint);

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .method(endpoint.httpMethod().name(), HttpRequest.BodyPublishers.noBody())
                            .uri(URI.create(targetUrl))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());

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

    private static String unexpectedStatusCode(InstanceId instanceId, ActuatorEndpoint endpoint, int statusCode) {
        return "Endpoint '%s' when invoked on instance '%s' did not respond with 2xx response, but with %d"
                .formatted(endpoint.path(), instanceId.instanceId(), statusCode);
    }
}
