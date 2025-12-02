package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.api.ConfigPropsFeed.Property;
import com.nucleonforge.axile.common.api.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigPropsJacksonMessageDeserializationStrategy}. The json for deserialization was taken from
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html">official doc</a>
 *
 * @author Sergey Cherkasov
 */
public class ConfigPropsJacksonMessageDeserializationStrategyTest {

    private final ConfigPropsJacksonMessageDeserializationStrategy subject =
            new ConfigPropsJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeAxileConfigPropsFeed() {
        // language=json
        String response =
                """
                {
                  "contexts" : {
                    "application1" : {
                      "beans" : {
                        "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean1" : {
                          "prefix" : "management.endpoints.web.cors",
                          "properties": [
                            { "name": "allowedOrigins", "value": null , "validationMessage": null},
                            { "name": "maxAge", "value": "PT30M", "validationMessage": "Should set PT10M" },
                            { "name": "exposedHeaders", "value": null , "validationMessage": null},
                            { "name": "allowedOriginPatterns", "value": null , "validationMessage": null},
                            { "name": "allowedHeaders", "value": null , "validationMessage": null},
                            { "name": "allowedMethods", "value": null , "validationMessage": null}
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
                        "org.springframework.boot.autoconfigure.web.Bean2" : {
                          "prefix" : "spring.web",
                          "properties": [
                            { "name": "localeResolver", "value": "ACCEPT_HEADER" , "validationMessage": null},
                            { "name": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/", "validationMessage": null},
                            { "name": "resources.staticLocations[1]", "value": "classpath:/resources/", "validationMessage": null},
                            { "name": "resources.staticLocations[2]", "value": "classpath:/static/", "validationMessage": null},
                            { "name": "resources.staticLocations[3]", "value": "classpath:/public/", "validationMessage": null},
                            { "name": "resources.addMappings", "value": "true", "validationMessage": null },
                            { "name": "resources.chain.cache", "value": "true", "validationMessage": null},
                            { "name": "resources.chain.compressed", "value": "false", "validationMessage": null},
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
                    }
                   }
                  }
                """;

        // when.
        ConfigPropsFeed configPropsFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        Map<String, ConfigPropsFeed.Context> context = configPropsFeed.contexts();

        // bean1
        ConfigPropsFeed.Bean bean1 =
                getBeanByName(context, "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean1");

        // bean1 -> prefix
        assertThat(bean1.prefix()).isEqualTo("management.endpoints.web.cors");

        // bean1 -> properties
        assertThat(bean1.properties())
                .containsOnly(
                        new Property("allowedOrigins", null, null),
                        new Property("maxAge", "PT30M", "Should set PT10M"),
                        new Property("exposedHeaders", null, null),
                        new Property("allowedOriginPatterns", null, null),
                        new Property("allowedHeaders", null, null),
                        new Property("allowedMethods", null, null));

        // bean1 -> inputs
        assertThat(bean1.inputs())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", null),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean2
        ConfigPropsFeed.Bean bean2 = getBeanByName(context, "org.springframework.boot.autoconfigure.web.Bean2");

        // bean2 -> prefix
        assertThat(bean2.prefix()).isEqualTo("spring.web");

        // bean2 -> properties
        assertThat(bean2.properties())
                .containsOnly(
                        new Property("localeResolver", "ACCEPT_HEADER", null),
                        new Property("resources.staticLocations[0]", "classpath:/META-INF/resources/", null),
                        new Property("resources.staticLocations[1]", "classpath:/resources/", null),
                        new Property("resources.staticLocations[2]", "classpath:/static/", null),
                        new Property("resources.staticLocations[3]", "classpath:/public/", null),
                        new Property("resources.addMappings", "true", null),
                        new Property("resources.chain.cache", "true", null),
                        new Property("resources.chain.compressed", "false", null),
                        new Property("resources.chain.strategy.fixed.enabled", "false", null),
                        new Property("resources.chain.strategy.fixed.paths[0]", "/**", null),
                        new Property("resources.chain.strategy.content.enabled", "false", null),
                        new Property("resources.chain.strategy.content.paths[0]", "/**", null),
                        new Property("resources.cache.cachecontrol", null, null),
                        new Property("resources.cache.useLastModified", "true", null));

        // bean2 -> inputs
        assertThat(bean2.inputs())
                .containsOnly(
                        new KeyValue("localeResolver", null),
                        new KeyValue("resources.staticLocations[0]", null),
                        new KeyValue("resources.staticLocations[1]", null),
                        new KeyValue("resources.staticLocations[2]", null),
                        new KeyValue("resources.staticLocations[3]", null),
                        new KeyValue("resources.addMappings", null),
                        new KeyValue("resources.chain.cache", null),
                        new KeyValue("resources.chain.compressed", null),
                        new KeyValue("resources.chain.strategy.fixed.enabled", null),
                        new KeyValue("resources.chain.strategy.fixed.paths[0]", null),
                        new KeyValue("resources.chain.strategy.content.enabled", null),
                        new KeyValue("resources.chain.strategy.content.paths[0]", null),
                        new KeyValue("resources.cache.cachecontrol", null),
                        new KeyValue("resources.cache.useLastModified", null));
    }

    private static ConfigPropsFeed.Bean getBeanByName(Map<String, ConfigPropsFeed.Context> context, String beanName) {
        return context.values().stream()
                .map(ConfigPropsFeed.Context::beans)
                .findFirst()
                .map(beansMap -> beansMap.get(beanName))
                .get();
    }
}
