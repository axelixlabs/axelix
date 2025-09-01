package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.Main;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

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
@SpringBootTest(classes = Main.class)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class BeansEndpointProberTest {

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

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/vnd.spring-boot.actuator.v3+json"));
    }

    @Test
    void shouldReturnBeansFeed() {
        String instanceId = "test-instance";

        registry.register(
                createInstanceWithUrl(instanceId, mockWebServer.url("/").toString()));

        BeansFeed feed = beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();
        assertThat(feed.getContexts()).containsKey("application");

        BeansFeed.Context ctx = feed.getContexts().get("application");
        Map<String, BeansFeed.Bean> beans = ctx.getBeans();
        assertThat(beans).hasSize(3);

        BeansFeed.Bean jmxEndpoint = beans.get("jmxEndpointProperties");
        assertThat(jmxEndpoint.getScope()).isEqualTo("singleton");
        assertThat(jmxEndpoint.getType()).isEqualTo("JmxEndpointProperties");
        assertThat(jmxEndpoint.getAliases()).isEmpty();
        assertThat(jmxEndpoint.getDependencies()).isEmpty();

        BeansFeed.Bean jacksonBuilder = beans.get("jacksonObjectMapperBuilder");
        assertThat(jacksonBuilder.getScope()).isEqualTo("prototype");
        assertThat(jacksonBuilder.getType()).isEqualTo("Jackson2ObjectMapperBuilder");
        assertThat(jacksonBuilder.getAliases()).isEmpty();
        assertThat(jacksonBuilder.getDependencies())
                .containsExactlyInAnyOrder("JacksonObjectMapperBuilderConfiguration");

        BeansFeed.Bean testSessionBean = beans.get("testSessionBean");
        assertThat(testSessionBean.getScope()).isEqualTo("session");
        assertThat(testSessionBean.getType()).isEqualTo("TestSessionBean");
        assertThat(testSessionBean.getAliases()).containsExactly("sessionBeanForProberTest");
        assertThat(testSessionBean.getDependencies()).isEmpty();
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
