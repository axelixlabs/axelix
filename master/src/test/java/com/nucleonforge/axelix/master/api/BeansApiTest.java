/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axelix.master.utils.InvalidAuthScenario;
import com.nucleonforge.axelix.master.utils.TestObjectFactory;
import com.nucleonforge.axelix.master.utils.TestRestTemplateBuilder;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link BeansApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
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
              "autoConfigurationRef" : null,
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
              "autoConfigurationRef" :"HibernateJpaConfiguration#entityManagerFactoryBuilder",
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
                "enclosingClassName": "HibernateJpaConfiguration",
                "enclosingClassFullName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
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
              "autoConfigurationRef" : null,
              "dependencies": [],
              "isPrimary": false,
              "isLazyInit": false,
              "isConfigPropsBean": false,
              "qualifiers": [],
              "beanSource": {
                "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                "origin": "FACTORY_BEAN"
              }
            },
            {
              "beanName": "syntheticBeanDefinition",
              "scope": "singleton",
              "className": "SomeClass",
              "aliases": [],
              "autoConfigurationRef" : null,
              "proxyType" : "NO_PROXYING",
              "dependencies": [],
              "isPrimary": false,
              "isLazyInit": false,
              "isConfigPropsBean": false,
              "qualifiers": [],
              "beanSource": {
                  "origin": "SYNTHETIC_BEAN"
               }
            }
          ]
        }
        """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

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
                  "autoConfigurationRef" : null,
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
                  "aliases": [],
                  "autoConfigurationRef" : "HibernateJpaConfiguration#entityManagerFactoryBuilder",
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
                    "enclosingClassName": "HibernateJpaConfiguration",
                    "enclosingClassFullName": "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration",
                    "methodName": "entityManagerFactoryBuilder",
                    "origin": "BEAN_METHOD"
                  }
                },
                "testSessionBean": {
                  "scope": "session",
                  "type": "TestSessionBean",
                  "proxyType" : "NO_PROXYING",
                  "aliases": ["sessionBeanForProberTest"],
                  "autoConfigurationRef" : null,
                  "dependencies": [],
                  "isLazyInit": false,
                  "isPrimary": false,
                  "isConfigPropsBean": false,
                  "qualifiers": [],
                  "beanSource": {
                    "factoryBeanName": "org.springframework.data.repository.config.PropertiesBasedNamedQueriesFactoryBean",
                    "origin": "FACTORY_BEAN"
                  }
                },
                "syntheticBeanDefinition" : {
                  "scope": "singleton",
                  "type": "SomeClass",
                  "proxyType" : "NO_PROXYING",
                  "aliases": [],
                  "autoConfigurationRef" : null,
                  "dependencies": [],
                  "isPrimary": false,
                  "isLazyInit": false,
                  "isConfigPropsBean": false,
                  "qualifiers": [],
                  "beanSource": {
                      "origin": "SYNTHETIC_BEAN"
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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-beans")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONBeansFeed() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/beans/feed/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEANS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/beans/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/beans/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/api/axelix/beans/feed/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
