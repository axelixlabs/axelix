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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link BeansEndpointProber}.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class BeansEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private BeansEndpointProber beansEndpointProber;

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
              "contexts": {
                "application": {
                  "beans": {
                    "jmxEndpointProperties": {
                      "scope": "singleton",
                      "type": "JmxEndpointProperties",
                      "aliases": [],
                      "dependencies": []
                    },
                    "jacksonObjectMapperBuilder": {
                      "scope": "prototype",
                      "type": "Jackson2ObjectMapperBuilder",
                      "resource": "class path resource JacksonObjectMapperBuilderConfiguration.class]",
                      "aliases": [],
                      "dependencies": [
                      "JacksonObjectMapperBuilderConfiguration"
                      ]
                    },
                    "testSessionBean": {
                      "scope": "session",
                      "type": "TestSessionBean",
                      "resource": "class path resource [org.example.com]",
                      "aliases": ["sessionBeanForProberTest"],
                      "dependencies": []
                    }
                  }
                }
              }
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/beans")) {
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
    void shouldReturnBeansFeed() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        BeansFeed feed = beansEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();
        assertThat(feed.contexts()).containsKey("application");

        BeansFeed.Context ctx = feed.contexts().get("application");
        Map<String, BeansFeed.Bean> beans = ctx.beans();
        assertThat(beans).hasSize(3);

        BeansFeed.Bean jmxEndpoint = beans.get("jmxEndpointProperties");
        assertThat(jmxEndpoint.scope()).isEqualTo("singleton");
        assertThat(jmxEndpoint.type()).isEqualTo("JmxEndpointProperties");
        assertThat(jmxEndpoint.aliases()).isEmpty();
        assertThat(jmxEndpoint.dependencies()).isEmpty();

        BeansFeed.Bean jacksonBuilder = beans.get("jacksonObjectMapperBuilder");
        assertThat(jacksonBuilder.scope()).isEqualTo("prototype");
        assertThat(jacksonBuilder.type()).isEqualTo("Jackson2ObjectMapperBuilder");
        assertThat(jacksonBuilder.aliases()).isEmpty();
        assertThat(jacksonBuilder.dependencies()).containsExactlyInAnyOrder("JacksonObjectMapperBuilderConfiguration");

        BeansFeed.Bean testSessionBean = beans.get("testSessionBean");
        assertThat(testSessionBean.scope()).isEqualTo("session");
        assertThat(testSessionBean.type()).isEqualTo("TestSessionBean");
        assertThat(testSessionBean.aliases()).containsExactly("sessionBeanForProberTest");
        assertThat(testSessionBean.dependencies()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = "test-instance-unreachable";

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = "unregistered-instance";

        assertThatThrownBy(() -> beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
