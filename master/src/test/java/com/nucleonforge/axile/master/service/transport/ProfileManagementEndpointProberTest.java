package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.util.Map;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ProfileManagementEndpointProber}.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class ProfileManagementEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ProfileManagementEndpointProber profileManagementEndpointProber;

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
              "updated": true,
              "reason": "New profiles have been activated"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/profile-management")) {
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
    void shouldReturnProfileMutationResult() throws JsonProcessingException {
        registry.register(createInstanceWithUrl(
                activeInstanceId,
                mockWebServer.url(activeInstanceId + "/actuator").toString()));

        byte[] profile = new ObjectMapper().writeValueAsBytes("postgres");
        HttpPayload payload = HttpPayload.json(profile);

        ProfileMutationResult result = profileManagementEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        assertThat(result).isNotNull();
        assertThat(result.updated()).isTrue();
        assertThat(result.reason()).isEqualTo("New profiles have been activated");
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();
        String profile = "postgres";
        HttpPayload payload = new DefaultHttpPayload(Map.of("profiles", profile));

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> profileManagementEndpointProber.invoke(InstanceId.of(instanceId), payload))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(
                        () -> profileManagementEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
