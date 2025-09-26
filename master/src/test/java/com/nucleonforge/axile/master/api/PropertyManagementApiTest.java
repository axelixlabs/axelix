package com.nucleonforge.axile.master.api;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.request.PropertyUpdatedRequest;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PropertyManagementApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PropertyManagementApiTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

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
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/property-management")) {
                    return new MockResponse()
                            .setResponseCode(204)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnOkOnPropertyUpdate() {
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        registry.register(createInstanceWithUrl(
                activeInstanceId,
                mockWebServer.url(activeInstanceId + "/actuator").toString()));

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/property-management/{instanceId}/property",
                defaultEntity(request),
                Void.class,
                activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when updating property")
    void shouldReturnInternalServerErrorOnPropertyManagement() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "value");

        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/property-management/{instanceId}/property",
                defaultEntity(request),
                EndpointInvocationException.class,
                instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/property-management/{instanceId}/property",
                request,
                EndpointInvocationException.class,
                instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private HttpEntity<PropertyUpdatedRequest> defaultEntity(PropertyUpdatedRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }
}
