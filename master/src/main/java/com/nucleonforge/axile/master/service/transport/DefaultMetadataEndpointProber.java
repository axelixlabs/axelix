package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.AxileMetadata;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;

/**
 * Default implementation of {@link MetadataEndpointProber} which specifically works with the
 * {@link ActuatorEndpoints#METADATA} endpoint (/axile-metadata).
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Service
public class DefaultMetadataEndpointProber implements MetadataEndpointProber {

    private final MessageDeserializationStrategy<AxileMetadata> messageDeserializationStrategy;
    private final HttpClient httpClient;

    public DefaultMetadataEndpointProber(MessageDeserializationStrategy<AxileMetadata> messageDeserializationStrategy) {
        this.messageDeserializationStrategy = messageDeserializationStrategy;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public @NonNull AxileMetadata invoke(@NonNull String instanceActuatorUrl) throws EndpointInvocationException {

        String path = supports().path().expand(Map.of(), List.of());

        String url = instanceActuatorUrl + path;

        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return messageDeserializationStrategy.deserialize(response.body());
            } else {
                throw new EndpointInvocationException("Endpoint '%s' on instance '%s' responded with %d"
                        .formatted(supports().path(), instanceActuatorUrl, statusCode));
            }

        } catch (IOException | InterruptedException e) {
            throw new EndpointInvocationException(e);
        }
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METADATA;
    }
}
