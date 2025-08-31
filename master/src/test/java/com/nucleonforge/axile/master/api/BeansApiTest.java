package com.nucleonforge.axile.master.api;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.Main;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link BeansApi}.
 *
 * @since 28.08.2025
 * author Nikita Kirillov
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class BeansApiTest {

    private static final String EXPECTED_BEANS_JSON =
            // language=json
            """
            {
              "beans": [
                {
                  "beanName": "dispatcherServletRegistrationConfiguration",
                  "scope": "singleton",
                  "className": "DispatcherServletRegistrationConfiguration",
                  "aliases": [],
                  "dependencies": []
                },
                {
                  "beanName": "propertyPlaceholderAutoConfiguration",
                  "scope": "prototype",
                  "className": "PropertyPlaceholderAutoConfiguration",
                  "aliases": [],
                  "dependencies": []
                },
                {
                  "beanName": "dispatcherServletAutoConfiguration",
                  "scope": "session",
                  "className": "DispatcherServletAutoConfiguration",
                  "aliases": [],
                  "dependencies": []
                },
                {
                  "beanName": "discoveryClientHealthIndicator",
                  "scope": "request",
                  "className": "DiscoveryClientHealthIndicator",
                  "aliases": [
                    "clientHealthIndicator",
                    "healthIndicator"
                  ],
                  "dependencies": [
                    "DiscoveryLoadBalancerConfiguration",
                    "DiscoveryClientHealthIndicatorProperties"
                  ]
                }
              ]
            }
            """;

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
              "contexts" : {
                "application" : {
                  "beans" : {
                    "dispatcherServletRegistrationConfiguration" : {
                      "scope" : "singleton",
                      "type" : "DispatcherServletRegistrationConfiguration",
                      "aliases" : [ ],
                      "dependencies" : [ ]
                    },
                    "propertyPlaceholderAutoConfiguration" : {
                      "scope" : "prototype",
                      "type" : "PropertyPlaceholderAutoConfiguration",
                      "aliases" : [ ],
                      "dependencies" : [ ]
                    },
                    "dispatcherServletAutoConfiguration" : {
                      "scope" : "session",
                      "type" : "DispatcherServletAutoConfiguration",
                      "aliases" : [ ],
                      "dependencies" : [ ]
                    },
                    "discoveryClientHealthIndicator": {
                      "scope": "request",
                      "type": "DiscoveryClientHealthIndicator",
                      "resource": "class path resource [org/springframework/cloud/client/CommonsClientAutoConfiguration$DiscoveryLoadBalancerConfiguration.class]",
                      "aliases": ["clientHealthIndicator", "healthIndicator"],
                      "dependencies": ["DiscoveryLoadBalancerConfiguration", "DiscoveryClientHealthIndicatorProperties"]
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
    void shouldReturnJSONBeansFeed() {
        String instanceId = "test-instance-id";

        registry.register(
                createInstanceWithUrl(instanceId, mockWebServer.url("/").toString()));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/axile/api/beans/feed/{instanceId}", String.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEANS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = "test-instance-unreachable";

        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/axile/api/beans/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-beans-instance";

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/axile/api/beans/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
