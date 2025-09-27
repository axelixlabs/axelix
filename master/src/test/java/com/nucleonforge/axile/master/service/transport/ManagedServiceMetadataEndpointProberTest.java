package com.nucleonforge.axile.master.service.transport;

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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.ManagedServiceMetadata;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ManagedServiceMetadataEndpointProber}.
 *
 * @since 19.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class ManagedServiceMetadataEndpointProberTest {

    private static final String activeInstanceUrl = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private ManagedServiceMetadataEndpointProber metadataEndpointProber;

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
    void setUp() {
        // language=json
        String jsonResponse = """
            {
              "version": "1.0.0-SNAPSHOT"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceUrl + "/actuator/axile-metadata")) {
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
    void shouldReturnMetadata() throws EndpointInvocationException {
        String instanceUrl = mockWebServer.url(activeInstanceUrl).toString();
        ManagedServiceMetadata metadata =
                metadataEndpointProber.invoke(instanceUrl + "/actuator", NoHttpPayload.INSTANCE);

        assertThat(metadata).isNotNull();
        assertThat(metadata.version()).isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNot2xx() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                return new MockResponse().setResponseCode(500);
            }
        });

        String instanceUrl = mockWebServer.url(activeInstanceUrl).toString();
        assertThatThrownBy(() -> metadataEndpointProber.invoke(instanceUrl, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsInvalid() {
        String invalidUrl = "http://localhost:0/non-existent";

        assertThatThrownBy(() -> metadataEndpointProber.invoke(invalidUrl, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }
}
