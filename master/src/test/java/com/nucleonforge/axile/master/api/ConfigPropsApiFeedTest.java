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
 * Integration tests for {@link ConfigPropsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigPropsApiFeedTest {
    // language=json
    private static final String EXPECTED_BEANS_FEED_JSON =
            """
            {
          "beans": [
            {
              "beanName": "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
              "prefix": "management.endpoints.web.cors",
              "properties": [
                { "name": "allowedOrigins", "value": null, "validationMessage": null },
                { "name": "maxAge", "value": "PT30M", "validationMessage": null },
                { "name": "exposedHeaders", "value": null, "validationMessage": null },
                { "name": "allowedOriginPatterns", "value": null, "validationMessage": null },
                { "name": "allowedHeaders", "value": null, "validationMessage": null },
                { "name": "allowedMethods", "value": null, "validationMessage": null }
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
                { "name": "allowedOrigins", "value": null, "validationMessage": null },
                { "name": "maxAge", "value": "PT30M", "validationMessage": null },
                { "name": "exposedHeaders", "value": null, "validationMessage": null },
                { "name": "allowedOriginPatterns", "value": null, "validationMessage": null },
                { "name": "allowedHeaders", "value": null, "validationMessage": null },
                { "name": "allowedMethods", "value": null, "validationMessage": null }
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
                { "name": "pathMapping", "value": null, "validationMessage": null },
                { "name": "exposure.include[0]", "value": "*", "validationMessage": "Should separate endpoints explicitly in production." },
                { "name": "exposure.exclude", "value": null, "validationMessage": null },
                { "name": "basePath", "value": "/actuator", "validationMessage": null },
                { "name": "discovery.enabled", "value": "true", "validationMessage": null }
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
                { "name": "localeResolver", "value": "ACCEPT_HEADER", "validationMessage": null },
                { "name": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/", "validationMessage": null },
                { "name": "resources.staticLocations[1]", "value": "classpath:/resources/", "validationMessage": null },
                { "name": "resources.staticLocations[2]", "value": "classpath:/static/", "validationMessage": null },
                { "name": "resources.staticLocations[3]", "value": "classpath:/public/", "validationMessage": null },
                { "name": "resources.addMappings", "value": "true", "validationMessage": null },
                { "name": "resources.chain.cache", "value": "true", "validationMessage": null },
                { "name": "resources.chain.compressed", "value": "false", "validationMessage": null },
                { "name": "resources.chain.strategy.fixed.enabled", "value": "false", "validationMessage": null },
                { "name": "resources.chain.strategy.fixed.paths[0]", "value": "/**", "validationMessage": null },
                { "name": "resources.chain.strategy.content.enabled", "value": "false", "validationMessage": null },
                { "name": "resources.chain.strategy.content.paths[0]", "value": "/**", "validationMessage": null },
                { "name": "resources.cache.cachecontrol", "value": null, "validationMessage": null },
                { "name": "resources.cache.useLastModified", "value": "true", "validationMessage": null }
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
        String jsonBeansFeedResponse =
                """
                {
              "contexts" : {
                "application1" : {
                  "beans" : {
                    "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                      "prefix" : "management.endpoints.web.cors",
                      "properties": [
                        { "name": "allowedOrigins", "value": null, "validationMessage": null },
                        { "name": "maxAge", "value": "PT30M", "validationMessage": null },
                        { "name": "exposedHeaders", "value": null, "validationMessage": null },
                        { "name": "allowedOriginPatterns", "value": null, "validationMessage": null },
                        { "name": "allowedHeaders", "value": null, "validationMessage": null },
                        { "name": "allowedMethods", "value": null, "validationMessage": null }
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
                    "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties" : {
                      "prefix" : "management.endpoints.web",
                      "properties": [
                        { "name": "pathMapping", "value": null, "validationMessage": null },
                        { "name": "exposure.include[0]", "value": "*", "validationMessage": "Should separate endpoints explicitly in production." },
                        { "name": "exposure.exclude", "value": null, "validationMessage": null },
                        { "name": "basePath", "value": "/actuator", "validationMessage": null },
                        { "name": "discovery.enabled", "value": "true", "validationMessage": null }
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
                    "org.springframework.boot.autoconfigure.web.WebProperties" : {
                      "prefix" : "spring.web",
                      "properties": [
                        { "name": "localeResolver", "value": "ACCEPT_HEADER", "validationMessage": null },
                        { "name": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/", "validationMessage": null },
                        { "name": "resources.staticLocations[1]", "value": "classpath:/resources/", "validationMessage": null },
                        { "name": "resources.staticLocations[2]", "value": "classpath:/static/", "validationMessage": null },
                        { "name": "resources.staticLocations[3]", "value": "classpath:/public/", "validationMessage": null },
                        { "name": "resources.addMappings", "value": "true", "validationMessage": null },
                        { "name": "resources.chain.cache", "value": "true", "validationMessage": null },
                        { "name": "resources.chain.compressed", "value": "false", "validationMessage": null },
                        { "name": "resources.chain.strategy.fixed.enabled", "value": "false", "validationMessage": null },
                        { "name": "resources.chain.strategy.fixed.paths[0]", "value": "/**", "validationMessage": null },
                        { "name": "resources.chain.strategy.content.enabled", "value": "false", "validationMessage": null },
                        { "name": "resources.chain.strategy.content.paths[0]", "value": "/**", "validationMessage": null },
                        { "name": "resources.cache.cachecontrol", "value": null, "validationMessage": null },
                        { "name": "resources.cache.useLastModified", "value": "true", "validationMessage": null }
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
                  }
                },
                "application2" : {
                  "beans" : {
                    "org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                      "prefix" : "management.endpoints.web.cors",
                      "properties": [
                        { "name": "allowedOrigins", "value": null, "validationMessage": null },
                        { "name": "maxAge", "value": "PT30M", "validationMessage": null },
                        { "name": "exposedHeaders", "value": null, "validationMessage": null },
                        { "name": "allowedOriginPatterns", "value": null, "validationMessage": null },
                        { "name": "allowedHeaders", "value": null, "validationMessage": null },
                        { "name": "allowedMethods", "value": null, "validationMessage": null }
                      ],
                      "inputs": [
                        { "key": "allowedOrigins", "value": null },
                        { "key": "maxAge", "value": null },
                        { "key": "exposedHeaders", "value": null },
                        { "key": "allowedOriginPatterns", "value": null },
                        { "key": "allowedHeaders", "value": null },
                        { "key": "allowedMethods", "value": null }
                      ]
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-configprops")) {
                    return new MockResponse()
                            .setBody(jsonBeansFeedResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnJSONConfigPropsFeed() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        // when.
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/configprops/feed/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEANS_FEED_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when calling beans feed")
    void shouldReturnInternalServerErrorOnConfigpropsFeed() {
        // when.
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/configprops/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        // when.
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/configprops/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
