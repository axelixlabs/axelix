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
import com.nucleonforge.axile.common.api.BeansFeed.ProxyType;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.common.api.BeansFeed.Bean;
import static com.nucleonforge.axile.common.api.BeansFeed.BeanMethod;
import static com.nucleonforge.axile.common.api.BeansFeed.ComponentVariant;
import static com.nucleonforge.axile.common.api.BeansFeed.Context;
import static com.nucleonforge.axile.common.api.BeansFeed.FactoryBean;
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
              "parentId": null,
              "beans": {
                "jmxEndpointProperties": {
                  "scope": "singleton",
                  "type": "JmxEndpointProperties",
                  "proxyType" : "CGLIB",
                  "aliases": [],
                  "dependencies": [],
                  "isLazyInit": false,
                  "isPrimary": false,
                  "qualifiers": [],
                  "beanSource": {
                     "origin": "COMPONENT_ANNOTATION"
                  }
                },
                "jacksonObjectMapperBuilder": {
                  "scope": "prototype",
                  "type": "Jackson2ObjectMapperBuilder",
                  "proxyType" : "JDK_PROXY",
                  "resource": "class path resource JacksonObjectMapperBuilderConfiguration.class",
                  "aliases": [],
                  "dependencies": [
                    "JacksonObjectMapperBuilderConfiguration"
                  ],
                  "isLazyInit": true,
                  "isPrimary": true,
                  "qualifiers": ["primaryMapper"],
                  "beanSource": {
                    "enclosingClassName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
                    "methodName": "entityManagerFactoryBuilder",
                    "origin": "BEAN_METHOD"
                  }
                },
                "testSessionBean": {
                  "scope": "session",
                  "type": "TestSessionBean",
                  "proxyType" : "NO_PROXYING",
                  "resource": "class path resource [org.example.com]",
                  "aliases": ["sessionBeanForProberTest"],
                  "dependencies": [],
                  "isLazyInit": false,
                  "isPrimary": false,
                  "qualifiers": [],
                  "beanSource": {
                    "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                    "origin": "FACTORY_BEAN"
                  }
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
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        BeansFeed feed = beansEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();
        assertThat(feed.contexts()).containsKey("application");

        Context ctx = feed.contexts().get("application");
        assertThat(ctx.parentId()).isNull();

        Map<String, Bean> beans = ctx.beans();
        assertThat(beans).hasSize(3);

        Bean jmxEndpoint = beans.get("jmxEndpointProperties");
        assertThat(jmxEndpoint.scope()).isEqualTo("singleton");
        assertThat(jmxEndpoint.type()).isEqualTo("JmxEndpointProperties");
        assertThat(jmxEndpoint.aliases()).isEmpty();
        assertThat(jmxEndpoint.proxyType()).isEqualTo(ProxyType.CGLIB);
        assertThat(jmxEndpoint.dependencies()).isEmpty();
        assertThat(jmxEndpoint.isLazyInit()).isFalse();
        assertThat(jmxEndpoint.isPrimary()).isFalse();
        assertThat(jmxEndpoint.qualifiers()).isEmpty();
        assertThat(jmxEndpoint.beanSource()).isInstanceOf(ComponentVariant.class);

        Bean jacksonBuilder = beans.get("jacksonObjectMapperBuilder");
        assertThat(jacksonBuilder.scope()).isEqualTo("prototype");
        assertThat(jacksonBuilder.type()).isEqualTo("Jackson2ObjectMapperBuilder");
        assertThat(jacksonBuilder.proxyType()).isEqualTo(ProxyType.JDK_PROXY);
        assertThat(jacksonBuilder.aliases()).isEmpty();
        assertThat(jacksonBuilder.dependencies()).containsExactlyInAnyOrder("JacksonObjectMapperBuilderConfiguration");
        assertThat(jacksonBuilder.isLazyInit()).isTrue();
        assertThat(jacksonBuilder.isPrimary()).isTrue();
        assertThat(jacksonBuilder.qualifiers()).containsExactly("primaryMapper");

        assertThat(jacksonBuilder.beanSource()).isInstanceOf(BeanMethod.class);
        assertThat((BeanMethod) jacksonBuilder.beanSource())
                .extracting(BeanMethod::enclosingClassName)
                .isEqualTo("org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration");
        assertThat((BeanMethod) jacksonBuilder.beanSource())
                .extracting(BeanMethod::methodName)
                .isEqualTo("entityManagerFactoryBuilder");

        Bean testSessionBean = beans.get("testSessionBean");
        assertThat(testSessionBean.scope()).isEqualTo("session");
        assertThat(testSessionBean.type()).isEqualTo("TestSessionBean");
        assertThat(testSessionBean.proxyType()).isEqualTo(ProxyType.NO_PROXYING);
        assertThat(testSessionBean.aliases()).containsExactly("sessionBeanForProberTest");
        assertThat(testSessionBean.dependencies()).isEmpty();
        assertThat(testSessionBean.isLazyInit()).isFalse();
        assertThat(testSessionBean.isPrimary()).isFalse();
        assertThat(testSessionBean.qualifiers()).isEmpty();

        assertThat(testSessionBean.beanSource()).isInstanceOf(FactoryBean.class);
        assertThat((FactoryBean) testSessionBean.beanSource())
                .extracting(FactoryBean::factoryBeanName)
                .isEqualTo("org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean");
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
