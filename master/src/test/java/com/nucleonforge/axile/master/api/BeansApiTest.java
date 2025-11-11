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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link BeansApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeansApiTest {

    private static final String EXPECTED_BEANS_JSON =
            // language=json
            """
        {
          "beans": [
            {
              "beanName": "jmxEndpointProperties",
              "scope": "singleton",
              "className": "JmxEndpointProperties",
              "aliases": [],
              "proxyType" : "CGLIB",
              "dependencies": [],
              "isPrimary": false,
              "isLazyInit": false,
              "isConfigPropsBean": true,
              "qualifiers": [],
              "beanSource": {
                  "origin": "COMPONENT_ANNOTATION"
               }
            },
            {
              "beanName": "jacksonObjectMapperBuilder",
              "scope": "prototype",
              "className": "Jackson2ObjectMapperBuilder",
              "aliases": [],
              "proxyType" : "JDK_PROXY",
              "dependencies": [
                {
                  "name": "JacksonObjectMapperBuilderConfiguration",
                  "isConfigPropsDependency": true
                },
                {
                  "name": "org.springframework.boot.autoconfigure.orm.jpa.JpaProperties",
                  "isConfigPropsDependency": true
                }
              ],
              "isPrimary": true,
              "isLazyInit": true,
              "isConfigPropsBean": true,
              "qualifiers": ["primaryMapper"],
              "beanSource": {
                "enclosingClassName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
                "methodName": "entityManagerFactoryBuilder",
                "origin": "BEAN_METHOD"
              }
            },
            {
              "beanName": "testSessionBean",
              "scope": "session",
              "className": "TestSessionBean",
              "proxyType" : "NO_PROXYING",
              "aliases": ["sessionBeanForProberTest"],
              "dependencies": [],
              "isPrimary": false,
              "isLazyInit": false,
              "isConfigPropsBean": false,
              "qualifiers": [],
              "beanSource": {
                "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                "origin": "FACTORY_BEAN"
              }
            }
          ]
        }
        """;

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
        // language=json
        String jsonResponse =
                """
        {
          "contexts": {
            "application": {
              "parentId": null,
              "beans": {
                "management.prefix-jmxEndpointProperties": {
                  "scope": "singleton",
                  "type": "JmxEndpointProperties",
                  "proxyType" : "CGLIB",
                  "aliases": [],
                  "dependencies": [],
                  "isLazyInit": false,
                  "isPrimary": false,
                  "isConfigPropsBean": true,
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
                    {
                      "name": "some.prefix-JacksonObjectMapperBuilderConfiguration",
                      "isConfigPropsDependency": true
                    },
                    {
                     "name": "prefix-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties",
                     "isConfigPropsDependency": true
                   }
                 ],
                  "isLazyInit": true,
                  "isPrimary": true,
                  "isConfigPropsBean": true,
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
                  "isConfigPropsBean": false,
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
    void shouldReturnJSONBeansFeed() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/beans/feed/{instanceId}", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEANS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<?> response =
                restTemplate.getForEntity("/api/axile/beans/feed/{instanceId}", Void.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/beans/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
