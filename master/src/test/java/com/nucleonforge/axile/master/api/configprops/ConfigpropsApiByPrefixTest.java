package com.nucleonforge.axile.master.api.configprops;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.domain.InstanceId;
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
public class ConfigpropsApiByPrefixTest {
    // language=json
    private static final String EXPECTED_BEAN_BY_PREFIX_JSON =
            """
        {
          "beans": [
            {
              "beanName": "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties",
              "prefix": "spring.jackson",
              "properties": [
                { "key": "serialization.INDENT_OUTPUT", "value": "true" },
                { "key": "defaultPropertyInclusion", "value": "NON_NULL" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
              ],
              "inputs": [
                { "key": "serialization.INDENT_OUTPUT.value", "value": "true" },
                { "key": "serialization.INDENT_OUTPUT.origin", "value": "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "defaultPropertyInclusion.value", "value": "non_null" },
                { "key": "defaultPropertyInclusion.origin", "value": "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
              ]
            }
          ]
        }
        """;

    // language=json
    private static final String EXPECTED_DOUBLE_CONTEXT_BEAN_BY_PREFIX_JSON =
            """
        {
          "beans": [
            {
              "beanName": "spring.web-org.springframework.boot.autoconfigure.web.WebProperties",
              "prefix": "spring.web",
              "properties": [
                { "key": "serialization.INDENT_OUTPUT", "value": "true" },
                { "key": "defaultPropertyInclusion", "value": "NON_NULL" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
              ],
              "inputs": [
                { "key": "serialization.INDENT_OUTPUT.value", "value": "true" },
                { "key": "serialization.INDENT_OUTPUT.origin", "value": "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "defaultPropertyInclusion.value", "value": "non_null" },
                { "key": "defaultPropertyInclusion.origin", "value": "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
              ]
            },
            {
              "beanName": "spring.web-org.springframework.boot.autoconfigure.web.WebProperties",
              "prefix": "spring.web",
              "properties": [
                { "key": "serialization.INDENT_OUTPUT", "value": "true" },
                { "key": "defaultPropertyInclusion", "value": "NON_NULL" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
              ],
              "inputs": [
                { "key": "serialization.INDENT_OUTPUT.value", "value": "true" },
                { "key": "serialization.INDENT_OUTPUT.origin", "value": "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "defaultPropertyInclusion.value", "value": "non_null" },
                { "key": "defaultPropertyInclusion.origin", "value": "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\"" },
                { "key": "visibility", "value": null },
                { "key": "parser", "value": null },
                { "key": "deserialization", "value": null },
                { "key": "generator", "value": null },
                { "key": "mapper", "value": null }
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
        String jsonSingleBeanByPrefixResponse =
                """
                    {
                  "contexts" : {
                    "application" : {
                      "beans" : {
                        "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties" : {
                          "prefix" : "spring.jackson",
                          "properties" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : true
                            },
                            "defaultPropertyInclusion" : "NON_NULL",
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
                          },
                          "inputs" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : {
                                "value" : "true",
                                "origin" : "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\""
                              }
                            },
                            "defaultPropertyInclusion" : {
                              "value" : "non_null",
                              "origin" : "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\""
                            },
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
                          }
                        }
                      }
                    }
                  }
                                }
                """;

        // language=json
        String jsonDoubleContextBeanByPrefixResponse =
                """
                {
                  "contexts" : {
                    "application1" : {
                      "beans" : {
                        "spring.web-org.springframework.boot.autoconfigure.web.WebProperties" : {
                          "prefix" : "spring.web",
                          "properties" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : true
                            },
                            "defaultPropertyInclusion" : "NON_NULL",
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
                          },
                          "inputs" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : {
                                "value" : "true",
                                "origin" : "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\""
                              }
                            },
                            "defaultPropertyInclusion" : {
                              "value" : "non_null",
                              "origin" : "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\""
                            },
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
                          }
                        }
                      }
                    },
                    "application2" : {
                      "beans" : {
                        "spring.web-org.springframework.boot.autoconfigure.web.WebProperties" : {
                          "prefix" : "spring.web",
                          "properties" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : true
                            },
                            "defaultPropertyInclusion" : "NON_NULL",
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
                          },
                          "inputs" : {
                            "serialization" : {
                              "INDENT_OUTPUT" : {
                                "value" : "true",
                                "origin" : "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\""
                              }
                            },
                            "defaultPropertyInclusion" : {
                              "value" : "non_null",
                              "origin" : "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\""
                            },
                            "visibility" : { },
                            "parser" : { },
                            "deserialization" : { },
                            "generator" : { },
                            "mapper" : { }
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

                if (path.equals("/" + activeInstanceId + "/configprops/spring.jackson")) {
                    return new MockResponse()
                            .setBody(jsonSingleBeanByPrefixResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/configprops/spring.web")) {
                    return new MockResponse()
                            .setBody(jsonDoubleContextBeanByPrefixResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONConfigpropsSingleBeanByPrefix() {
        // when.
        String prefix = "spring.jackson";
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/configprops/{instanceId}/beans/{prefix}", String.class, activeInstanceId, prefix);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_BEAN_BY_PREFIX_JSON);
    }

    @Test
    void shouldReturnJSONConfigpropsDoubleContextBeanByPrefix() {
        // when.
        String prefix = "spring.web";
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/configprops/{instanceId}/beans/{prefix}", String.class, activeInstanceId, prefix);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DOUBLE_CONTEXT_BEAN_BY_PREFIX_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when calling a beans by prefix")
    void shouldReturnInternalServerErrorOnBeanByPrefix() {
        // when.
        String prefix = "spring.jackson";
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/configprops/{instanceId}/beans/{prefix}", String.class, instanceId, prefix);

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
