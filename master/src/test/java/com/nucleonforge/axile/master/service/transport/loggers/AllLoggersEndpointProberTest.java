package com.nucleonforge.axile.master.service.transport.loggers;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AllLoggersEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class AllLoggersEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private AllLoggersEndpointProber allLoggersEndpointProber;

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
                      "levels" : [ "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" ],
                      "loggers" : {
                        "ROOT" : {
                          "configuredLevel" : "INFO",
                          "effectiveLevel" : "INFO"
                        },
                        "com.example" : {
                          "configuredLevel" : "DEBUG",
                          "effectiveLevel" : "DEBUG"
                        },
                        "org" : {
                          "effectiveLevel" : "INFO"
                        }
                      },
                      "groups" : {
                        "test" : {
                          "configuredLevel" : "INFO",
                          "members" : [ "test.member1", "test.member2" ]
                        },
                        "web" : {
                          "members" : [ "org.springframework.core.codec", "org.springframework.http", "org.springframework.web", "org.springframework.boot.actuate.endpoint.web", "org.springframework.boot.web.servlet.ServletContextInitializerBeans" ]
                        },
                        "sql" : {
                          "members" : [ "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener" ]
                        }
                      }
                    }
                    """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/loggers")) {
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
    void shouldReturnGroupLogger() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when.
        ServiceLoggers loggers =
                allLoggersEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // levels
        assertThat(loggers.levels())
                .containsExactlyInAnyOrder("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // logger
        Map<String, LoggerLevels> logger = loggers.loggers();
        assertThat(logger).hasSize(3);

        // logger -> "ROOT"
        assertThat(logger.get("ROOT").configuredLevel()).isEqualTo("INFO");
        assertThat(logger.get("ROOT").effectiveLevel()).isEqualTo("INFO");

        // logger -> "com.example"
        assertThat(logger.get("com.example").configuredLevel()).isEqualTo("DEBUG");
        assertThat(logger.get("com.example").effectiveLevel()).isEqualTo("DEBUG");

        // logger -> "org"
        assertThat(logger.get("org").configuredLevel()).isNull();
        assertThat(logger.get("org").effectiveLevel()).isEqualTo("INFO");

        // group
        Map<String, LoggerGroup> group = loggers.groups();
        assertThat(group).hasSize(3);

        // group -> "test"
        assertThat(group.get("test").configuredLevel()).isEqualTo("INFO");
        assertThat(group.get("test").members()).containsExactlyInAnyOrder("test.member1", "test.member2");

        // group -> "web"
        assertThat(group.get("web").configuredLevel()).isNull();
        assertThat(group.get("web").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.core.codec",
                        "org.springframework.http",
                        "org.springframework.web",
                        "org.springframework.boot.actuate.endpoint.web",
                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans");

        // group -> "sql"
        assertThat(group.get("sql").configuredLevel()).isNull();
        assertThat(group.get("sql").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener");
    }
}
