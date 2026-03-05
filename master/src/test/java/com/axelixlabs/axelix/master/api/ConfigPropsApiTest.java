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
package com.axelixlabs.axelix.master.api;

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

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.ConfigPropsApi;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigPropsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigPropsApiTest {
    // language=json
    private static final String EXPECTED_BEANS_FEED_JSON = """
            {
          "beans": [
            {
              "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
              "prefix": "management.endpoints.web.cors",
              "properties": [
                { "key": "allowedOrigins", "value": null },
                { "key": "maxAge", "value": "PT30M" },
                { "key": "exposedHeaders", "value": null },
                { "key": "allowedOriginPatterns", "value": null },
                { "key": "allowedHeaders", "value": null },
                { "key": "allowedMethods", "value": null }
              ],
              "inputs": [
                { "key": "allowedOrigins", "value": null },
                { "key": "maxAge", "value": null },
                { "key": "exposedHeaders", "value": null },
                { "key": "allowedOriginPatterns", "value": null },
                { "key": "allowedHeaders", "value": null },
                { "key": "allowedMethods", "value": null }
              ]
            },
            {
              "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
              "prefix": "management.endpoints.web.cors",
              "properties": [
                { "key": "allowedOrigins", "value": null },
                { "key": "maxAge", "value": "PT30M" },
                { "key": "exposedHeaders", "value": null },
                { "key": "allowedOriginPatterns", "value": null },
                { "key": "allowedHeaders", "value": null },
                { "key": "allowedMethods", "value": null }
              ],
              "inputs": [
                { "key": "allowedOrigins", "value": null },
                { "key": "maxAge", "value": null },
                { "key": "exposedHeaders", "value": null },
                { "key": "allowedOriginPatterns", "value": null },
                { "key": "allowedHeaders", "value": null },
                { "key": "allowedMethods", "value": null }
              ]
            },
            {
              "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
              "prefix": "management.endpoints.web",
              "properties": [
                { "key": "pathMapping", "value": null },
                { "key": "exposure.include[0]", "value": "*" },
                { "key": "exposure.exclude", "value": null },
                { "key": "basePath", "value": "/actuator" },
                { "key": "discovery.enabled", "value": "true" }
              ],
              "inputs": [
                { "key": "pathMapping", "value": null },
                { "key": "exposure.include[0].value", "value": "*" },
                { "key": "exposure.include[0].origin", "value": "\\"management.endpoints.web.exposure.include\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "exposure.exclude", "value": null },
                { "key": "basePath", "value": null },
                { "key": "discovery.enabled", "value": null }
              ]
            },
            {
              "beanName": "org.springframework.boot.autoconfigure.web.WebProperties",
              "prefix": "spring.web",
              "properties": [
                { "key": "localeResolver", "value": "ACCEPT_HEADER" },
                { "key": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/" },
                { "key": "resources.staticLocations[1]", "value": "classpath:/resources/" },
                { "key": "resources.staticLocations[2]", "value": "classpath:/static/" },
                { "key": "resources.staticLocations[3]", "value": "classpath:/public/" },
                { "key": "resources.addMappings", "value": "true" },
                { "key": "resources.chain.cache", "value": "true" },
                { "key": "resources.chain.compressed", "value": "false" },
                { "key": "resources.chain.strategy.fixed.enabled", "value": "false" },
                { "key": "resources.chain.strategy.fixed.paths[0]", "value": "/**" },
                { "key": "resources.chain.strategy.content.enabled", "value": "false" },
                { "key": "resources.chain.strategy.content.paths[0]", "value": "/**" },
                { "key": "resources.cache.cachecontrol", "value": null },
                { "key": "resources.cache.useLastModified", "value": "true" }
              ],
              "inputs": [
                { "key": "localeResolver", "value": null },
                { "key": "resources.staticLocations[0]", "value": null },
                { "key": "resources.staticLocations[1]", "value": null },
                { "key": "resources.staticLocations[2]", "value": null },
                { "key": "resources.staticLocations[3]", "value": null },
                { "key": "resources.addMappings", "value": null },
                { "key": "resources.chain.cache", "value": null },
                { "key": "resources.chain.compressed", "value": null },
                { "key": "resources.chain.strategy.fixed.enabled", "value": null },
                { "key": "resources.chain.strategy.fixed.paths[0]", "value": null },
                { "key": "resources.chain.strategy.content.enabled", "value": null },
                { "key": "resources.chain.strategy.content.paths[0]", "value": null },
                { "key": "resources.cache.cachecontrol", "value": null },
                { "key": "resources.cache.useLastModified", "value": null }
              ]
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
        String jsonBeansFeedResponse = """
              {
            "beans": [
              {
                "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
                "prefix": "management.endpoints.web.cors",
                "properties": [
                  { "key": "allowedOrigins", "value": null },
                  { "key": "maxAge", "value": "PT30M" },
                  { "key": "exposedHeaders", "value": null },
                  { "key": "allowedOriginPatterns", "value": null },
                  { "key": "allowedHeaders", "value": null },
                  { "key": "allowedMethods", "value": null }
                ],
                "inputs": [
                  { "key": "allowedOrigins", "value": null },
                  { "key": "maxAge", "value": null },
                  { "key": "exposedHeaders", "value": null },
                  { "key": "allowedOriginPatterns", "value": null },
                  { "key": "allowedHeaders", "value": null },
                  { "key": "allowedMethods", "value": null }
                ]
              },
              {
                "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
                "prefix": "management.endpoints.web.cors",
                "properties": [
                  { "key": "allowedOrigins", "value": null },
                  { "key": "maxAge", "value": "PT30M" },
                  { "key": "exposedHeaders", "value": null },
                  { "key": "allowedOriginPatterns", "value": null },
                  { "key": "allowedHeaders", "value": null },
                  { "key": "allowedMethods", "value": null }
                ],
                "inputs": [
                  { "key": "allowedOrigins", "value": null },
                  { "key": "maxAge", "value": null },
                  { "key": "exposedHeaders", "value": null },
                  { "key": "allowedOriginPatterns", "value": null },
                  { "key": "allowedHeaders", "value": null },
                  { "key": "allowedMethods", "value": null }
                ]
              },
              {
                "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
                "prefix": "management.endpoints.web",
                "properties": [
                  { "key": "pathMapping", "value": null },
                  { "key": "exposure.include[0]", "value": "*" },
                  { "key": "exposure.exclude", "value": null },
                  { "key": "basePath", "value": "/actuator" },
                  { "key": "discovery.enabled", "value": "true" }
                ],
                "inputs": [
                  { "key": "pathMapping", "value": null },
                  { "key": "exposure.include[0].value", "value": "*" },
                  { "key": "exposure.include[0].origin", "value": "\\"management.endpoints.web.exposure.include\\" from property source \\"Inlined Test Properties\\"" },
                  { "key": "exposure.exclude", "value": null },
                  { "key": "basePath", "value": null },
                  { "key": "discovery.enabled", "value": null }
                ]
              },
              {
                "beanName": "org.springframework.boot.autoconfigure.web.WebProperties",
                "prefix": "spring.web",
                "properties": [
                  { "key": "localeResolver", "value": "ACCEPT_HEADER" },
                  { "key": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/" },
                  { "key": "resources.staticLocations[1]", "value": "classpath:/resources/" },
                  { "key": "resources.staticLocations[2]", "value": "classpath:/static/" },
                  { "key": "resources.staticLocations[3]", "value": "classpath:/public/" },
                  { "key": "resources.addMappings", "value": "true" },
                  { "key": "resources.chain.cache", "value": "true" },
                  { "key": "resources.chain.compressed", "value": "false" },
                  { "key": "resources.chain.strategy.fixed.enabled", "value": "false" },
                  { "key": "resources.chain.strategy.fixed.paths[0]", "value": "/**" },
                  { "key": "resources.chain.strategy.content.enabled", "value": "false" },
                  { "key": "resources.chain.strategy.content.paths[0]", "value": "/**" },
                  { "key": "resources.cache.cachecontrol", "value": null },
                  { "key": "resources.cache.useLastModified", "value": "true" }
                ],
                "inputs": [
                  { "key": "localeResolver", "value": null },
                  { "key": "resources.staticLocations[0]", "value": null },
                  { "key": "resources.staticLocations[1]", "value": null },
                  { "key": "resources.staticLocations[2]", "value": null },
                  { "key": "resources.staticLocations[3]", "value": null },
                  { "key": "resources.addMappings", "value": null },
                  { "key": "resources.chain.cache", "value": null },
                  { "key": "resources.chain.compressed", "value": null },
                  { "key": "resources.chain.strategy.fixed.enabled", "value": null },
                  { "key": "resources.chain.strategy.fixed.paths[0]", "value": null },
                  { "key": "resources.chain.strategy.content.enabled", "value": null },
                  { "key": "resources.chain.strategy.content.paths[0]", "value": null },
                  { "key": "resources.cache.cachecontrol", "value": null },
                  { "key": "resources.cache.useLastModified", "value": null }
                ]
              }
            ]
          }
          """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-configprops")) {
                    return new MockResponse()
                            .setBody(jsonBeansFeedResponse)
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
    void shouldReturnJSONConfigPropsFeed() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/external/configprops/feed/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEANS_FEED_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when calling beans feed")
    void shouldReturnInternalServerErrorOnConfigpropsFeed() {
        // when.
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/external/configprops/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        // when.
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/external/configprops/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/api/external/configprops/feed/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
