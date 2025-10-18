package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.util.List;
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
import org.springframework.http.MediaType;

import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpHeader;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.request.PropertyUpdatedRequest;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link PropertyManagementEndpointProber}.
 *
 * @since 26.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class PropertyManagementEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private PropertyManagementEndpointProber propertyManagementEndpointProber;

    @Autowired
    private JacksonMessageSerializationStrategy serializationStrategy;

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
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/property-management")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldInvokePropertyManagementEndpointSuccessfully() {
        registry.register(createInstanceWithUrl(
                activeInstanceId,
                mockWebServer.url(activeInstanceId + "/actuator").toString()));

        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        HttpHeader header = new HttpHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        List<HttpHeader> headers = List.of(header);
        HttpPayload payload = new DefaultHttpPayload(headers, serializationStrategy.serialize(request));

        assertThatCode(() -> propertyManagementEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");
        HttpHeader header = new HttpHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        List<HttpHeader> headers = List.of(header);
        HttpPayload payload = new DefaultHttpPayload(headers, serializationStrategy.serialize(request));

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> propertyManagementEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");
        HttpHeader header = new HttpHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        List<HttpHeader> headers = List.of(header);
        HttpPayload payload = new DefaultHttpPayload(headers, serializationStrategy.serialize(request));

        assertThatThrownBy(() -> propertyManagementEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
