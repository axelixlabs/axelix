package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.api.ConfigpropsFeed.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigpropsJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html">official doc</a>
 *
 * @author Sergey Cherkasov
 */
public class ConfigpropsJacksonMessageDeserializationStrategyTest {

    private final ConfigpropsJacksonMessageDeserializationStrategy subject =
            new ConfigpropsJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeConfigpropsFeed() {
        // when.
        // language=json
        String response =
                """
            {
                  "contexts" : {
                    "application" : {
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
                    }
                  }
                }
            """;

        // then.
        ConfigpropsFeed configpropsFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(configpropsFeed.contexts()).hasEntrySatisfying("application", context -> {
            assertThat(context.beans()).hasSize(3);

            shouldDeserializeConfigpropsBeanFirst(context);
            shouldDeserializeConfigpropsBeanSecond(context);
            shouldDeserializeConfigpropsBeanThree(context);
        });
    }

    private void shouldDeserializeConfigpropsBeanFirst(ConfigpropsFeed.Context context) {
        Bean bean = context.beans()
                .get(
                        "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties");

        // prefix
        assertThat(bean.prefix()).isEqualTo("management.endpoints.web.cors");

        // properties
        assertThat(bean.properties().get("allowedOrigins")).isEqualTo(List.of());
        assertThat(bean.properties().get("maxAge")).isEqualTo("PT30M");
        assertThat(bean.properties().get("exposedHeaders")).isEqualTo(List.of());
        assertThat(bean.properties().get("allowedOriginPatterns")).isEqualTo(List.of());
        assertThat(bean.properties().get("allowedHeaders")).isEqualTo(List.of());
        assertThat(bean.properties().get("allowedMethods")).isEqualTo(List.of());

        // inputs
        assertThat(bean.inputs().get("allowedOrigins")).isEqualTo(List.of());
        assertThat(bean.inputs().get("maxAge")).isEqualTo(Map.of());
        assertThat(bean.inputs().get("exposedHeaders")).isEqualTo(List.of());
        assertThat(bean.inputs().get("allowedOriginPatterns")).isEqualTo(List.of());
        assertThat(bean.inputs().get("allowedHeaders")).isEqualTo(List.of());
        assertThat(bean.inputs().get("allowedMethods")).isEqualTo(List.of());
    }

    @SuppressWarnings("unchecked")
    private void shouldDeserializeConfigpropsBeanSecond(ConfigpropsFeed.Context context) {
        Bean bean = context.beans()
                .get(
                        "management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties");

        // prefix
        assertThat(bean.prefix()).isEqualTo("management.endpoints.web");

        // properties
        assertThat(bean.properties().get("pathMapping")).isEqualTo(Map.of());
        assertThat(bean.properties().get("basePath")).isEqualTo("/actuator");
        assertThat(bean.properties().get("discovery")).isEqualTo(Map.of("enabled", true));

        // properties -> "exposure"
        Map<String, Object> exposureProperties =
                (Map<String, Object>) bean.properties().get("exposure");
        assertThat(exposureProperties).containsEntry("include", List.of("*"));
        assertThat(exposureProperties).containsEntry("exclude", List.of());

        // inputs
        assertThat(bean.inputs().get("pathMapping")).isEqualTo(Map.of());
        assertThat(bean.inputs().get("basePath")).isEqualTo(Map.of());
        assertThat(bean.inputs().get("discovery")).isEqualTo(Map.of("enabled", Map.of()));

        // inputs -> "exposure"
        Map<String, Object> exposureInputs = (Map<String, Object>) bean.inputs().get("exposure");
        assertThat(exposureInputs).containsEntry("exclude", List.of());
        assertThat(exposureInputs)
                .containsEntry(
                        "include",
                        List.of(
                                Map.of(
                                        "value",
                                        "*",
                                        "origin",
                                        "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\"")));
    }

    @SuppressWarnings("unchecked")
    private void shouldDeserializeConfigpropsBeanThree(ConfigpropsFeed.Context context) {
        Bean bean = context.beans().get("spring.web-org.springframework.boot.autoconfigure.web.WebProperties");

        // prefix
        assertThat(bean.prefix()).isEqualTo("spring.web");

        // properties
        assertThat(bean.properties().get("localeResolver")).isEqualTo("ACCEPT_HEADER");

        // properties -> "resources"
        Map<String, Object> resourcesProperties =
                (Map<String, Object>) bean.properties().get("resources");
        assertThat(resourcesProperties).hasSize(4);
        assertThat(resourcesProperties)
                .containsEntry(
                        "staticLocations",
                        List.of(
                                "classpath:/META-INF/resources/",
                                "classpath:/resources/",
                                "classpath:/static/",
                                "classpath:/public/"));
        assertThat(resourcesProperties).containsEntry("addMappings", true);

        // properties -> "resources" -> "chain"
        Map<String, Object> resourcesPropertiesChain = (Map<String, Object>) resourcesProperties.get("chain");
        assertThat(resourcesPropertiesChain).containsEntry("cache", true);
        assertThat(resourcesPropertiesChain).containsEntry("compressed", false);

        // properties -> "resources" -> "chain" -> "strategy"
        Map<String, Object> resourcesPropertiesChainStrategy =
                (Map<String, Object>) resourcesPropertiesChain.get("strategy");
        assertThat(resourcesPropertiesChainStrategy)
                .containsEntry("fixed", Map.of("enabled", false, "paths", List.of("/**")));
        assertThat(resourcesPropertiesChainStrategy)
                .containsEntry("content", Map.of("enabled", false, "paths", List.of("/**")));

        // properties -> "resources" -> "cache"
        Map<String, Object> resourcesPropertiesCache = (Map<String, Object>) resourcesProperties.get("cache");
        assertThat(resourcesPropertiesCache).containsEntry("cachecontrol", Map.of());
        assertThat(resourcesPropertiesCache).containsEntry("useLastModified", true);

        // inputs
        assertThat(bean.inputs().get("localeResolver")).isEqualTo(Map.of());

        // inputs -> "resources"
        Map<String, Object> resourcesInputs =
                (Map<String, Object>) bean.inputs().get("resources");
        assertThat(resourcesInputs).hasSize(4);
        assertThat(resourcesInputs).containsEntry("staticLocations", List.of(Map.of(), Map.of(), Map.of(), Map.of()));
        assertThat(resourcesInputs).containsEntry("addMappings", Map.of());

        // inputs -> "resources" -> "chain"
        Map<String, Object> resourcesInputsChain = (Map<String, Object>) resourcesInputs.get("chain");
        assertThat(resourcesInputsChain).containsEntry("cache", Map.of());
        assertThat(resourcesInputsChain).containsEntry("compressed", Map.of());

        // inputs -> "resources" -> "chain" -> "strategy"
        Map<String, Object> resourcesInputsChainStrategy = (Map<String, Object>) resourcesInputsChain.get("strategy");
        assertThat(resourcesInputsChainStrategy)
                .containsEntry("fixed", Map.of("enabled", Map.of(), "paths", List.of(Map.of())));
        assertThat(resourcesInputsChainStrategy)
                .containsEntry("content", Map.of("enabled", Map.of(), "paths", List.of(Map.of())));

        // inputs -> "resources" -> "cache"
        Map<String, Object> resourcesInputsCache = (Map<String, Object>) resourcesInputs.get("cache");
        assertThat(resourcesInputsCache).containsEntry("cachecontrol", Map.of());
        assertThat(resourcesInputsCache).containsEntry("useLastModified", Map.of());
    }
}
