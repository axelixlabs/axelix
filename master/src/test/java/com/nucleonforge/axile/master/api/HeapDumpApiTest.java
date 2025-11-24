package com.nucleonforge.axile.master.api;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.service.export.HeapDumpAnonymizer;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link HeapDumpApi}.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HeapDumpApiTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static final byte[] mockHeapDump = "Mock HPROF binary data".getBytes();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @MockBean
    private HeapDumpAnonymizer heapDumpAnonymizer;

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
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                String base64Data = Base64.getEncoder().encodeToString(mockHeapDump);

                if (path.equals("/" + activeInstanceId + "/actuator/heapdump")) {
                    return new MockResponse()
                            .setBody(base64Data)
                            .addHeader("Content-Type", "application/octet-stream")
                            .setResponseCode(200);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        when(heapDumpAnonymizer.anonymize(any(Resource.class))).thenAnswer(invocation -> {
            Resource originalResource = invocation.getArgument(0);
            String base64Content = new String(originalResource.getInputStream().readAllBytes());
            byte[] decodedData = Base64.getDecoder().decode(base64Content);

            return new ByteArrayResource(decodedData) {
                @Override
                public String getFilename() {
                    return "heapdump.hprof";
                }
            };
        });
    }

    @Test
    void shouldReturnHeapDumpAsAttachment() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<byte[]> response =
                restTemplate.getForEntity("/api/axile/heapdump/{instanceId}", byte[].class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(contentDisposition).isNotNull();
        assertThat(contentDisposition).contains("attachment");
        assertThat(contentDisposition).contains("filename=\"heapdump.hprof\"");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(mockHeapDump);
    }

    @Test
    void shouldReturnInternalServerErrorWhenHeapDumpFails() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/heapdump/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<InstanceNotFoundException> response = restTemplate.getForEntity(
                "/api/axile/heapdump/{instanceId}", InstanceNotFoundException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
