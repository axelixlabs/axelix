package com.nucleonforge.axile.master.api.configprops;

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
import com.nucleonforge.axile.master.api.ConfigpropsApi;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigpropsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigpropsApiFeedTest {
    // language=json
    private static final String EXPECTED_BEANS_FEED_JSON =
            """
            {
          "beans": [
            {
              "beanName": "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
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
              "beanName": "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties",
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
              "beanName": "management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties",
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
              "beanName": "spring.web-org.springframework.boot.autoconfigure.web.WebProperties",
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
                    "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                      "prefix" : "management.endpoints.web.cors",
                      "properties" : {
                        "allowedOrigins" : [ ],
                        "maxAge" : "PT30M",
                        "exposedHeaders" : [ ],
                        "allowedOriginPatterns" : [ ],
                        "allowedHeaders" : [ ],
                        "allowedMethods" : [ ]
                      },
                      "inputs" : {
                        "allowedOrigins" : [ ],
                        "maxAge" : { },
                        "exposedHeaders" : [ ],
                        "allowedOriginPatterns" : [ ],
                        "allowedHeaders" : [ ],
                        "allowedMethods" : [ ]
                      }
                    },
                    "management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties" : {
                      "prefix" : "management.endpoints.web",
                      "properties" : {
                        "pathMapping" : { },
                        "exposure" : {
                          "include" : [ "*" ],
                          "exclude" : [ ]
                        },
                        "basePath" : "/actuator",
                        "discovery" : {
                          "enabled" : true
                        }
                      },
                      "inputs" : {
                        "pathMapping" : { },
                        "exposure" : {
                          "include" : [ {
                            "value" : "*",
                            "origin" : "\\"management.endpoints.web.exposure.include\\" from property source \\"Inlined Test Properties\\""
                          } ],
                          "exclude" : [ ]
                        },
                        "basePath" : { },
                        "discovery" : {
                          "enabled" : { }
                        }
                      }
                    },
                    "spring.web-org.springframework.boot.autoconfigure.web.WebProperties" : {
                      "prefix" : "spring.web",
                      "properties" : {
                        "localeResolver" : "ACCEPT_HEADER",
                        "resources" : {
                          "staticLocations" : [ "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" ],
                          "addMappings" : true,
                          "chain" : {
                            "cache" : true,
                            "compressed" : false,
                            "strategy" : {
                              "fixed" : {
                                "enabled" : false,
                                "paths" : [ "/**" ]
                              },
                              "content" : {
                                "enabled" : false,
                                "paths" : [ "/**" ]
                              }
                            }
                          },
                          "cache" : {
                            "cachecontrol" : { },
                            "useLastModified" : true
                          }
                        }
                      },
                      "inputs" : {
                        "localeResolver" : { },
                        "resources" : {
                          "staticLocations" : [ { }, { }, { }, { } ],
                          "addMappings" : { },
                          "chain" : {
                            "cache" : { },
                            "compressed" : { },
                            "strategy" : {
                              "fixed" : {
                                "enabled" : { },
                                "paths" : [ { } ]
                              },
                              "content" : {
                                "enabled" : { },
                                "paths" : [ { } ]
                              }
                            }
                          },
                          "cache" : {
                            "cachecontrol" : { },
                            "useLastModified" : { }
                          }
                        }
                      }
                    }
                  }
                },
                "application2" : {
                  "beans" : {
                    "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                      "prefix" : "management.endpoints.web.cors",
                      "properties" : {
                        "allowedOrigins" : [ ],
                        "maxAge" : "PT30M",
                        "exposedHeaders" : [ ],
                        "allowedOriginPatterns" : [ ],
                        "allowedHeaders" : [ ],
                        "allowedMethods" : [ ]
                      },
                      "inputs" : {
                        "allowedOrigins" : [ ],
                        "maxAge" : { },
                        "exposedHeaders" : [ ],
                        "allowedOriginPatterns" : [ ],
                        "allowedHeaders" : [ ],
                        "allowedMethods" : [ ]
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

                if (path.equals("/" + activeInstanceId + "/configprops")) {
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
    void shouldReturnJSONConfigpropsFeed() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

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
