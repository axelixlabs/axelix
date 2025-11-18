package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;
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
                        "org.springframework.boot.autoconfigure.web.Bean2" : {
                          "prefix" : "spring.web",
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
                      }
                    }
                   }
                  }
                """;

        // when.
        AxileConfigPropsFeed configPropsFeed = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        Map<String, AxileConfigPropsFeed.Context> context = configPropsFeed.contexts();

        // bean1
        AxileConfigPropsFeed.Bean bean1 =
                getBeanByName(context, "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean1");

        // bean1 -> prefix
        assertThat(bean1.prefix()).isEqualTo("management.endpoints.web.cors");

        // bean1 -> properties
        assertThat(bean1.properties())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", "PT30M"),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

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
        AxileConfigPropsFeed.Bean bean2 = getBeanByName(context, "org.springframework.boot.autoconfigure.web.Bean2");

        // bean2 -> prefix
        assertThat(bean2.prefix()).isEqualTo("spring.web");

        // bean2 -> properties
        assertThat(bean2.properties())
                .containsOnly(
                        new KeyValue("localeResolver", "ACCEPT_HEADER"),
                        new KeyValue("resources.staticLocations[0]", "classpath:/META-INF/resources/"),
                        new KeyValue("resources.staticLocations[1]", "classpath:/resources/"),
                        new KeyValue("resources.staticLocations[2]", "classpath:/static/"),
                        new KeyValue("resources.staticLocations[3]", "classpath:/public/"),
                        new KeyValue("resources.addMappings", "true"),
                        new KeyValue("resources.chain.cache", "true"),
                        new KeyValue("resources.chain.compressed", "false"),
                        new KeyValue("resources.chain.strategy.fixed.enabled", "false"),
                        new KeyValue("resources.chain.strategy.fixed.paths[0]", "/**"),
                        new KeyValue("resources.chain.strategy.content.enabled", "false"),
                        new KeyValue("resources.chain.strategy.content.paths[0]", "/**"),
                        new KeyValue("resources.cache.cachecontrol", null),
                        new KeyValue("resources.cache.useLastModified", "true"));

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

    private static AxileConfigPropsFeed.Bean getBeanByName(
            Map<String, AxileConfigPropsFeed.Context> context, String beanName) {
        return context.values().stream()
                .map(AxileConfigPropsFeed.Context::beans)
                .findFirst()
                .map(beansMap -> beansMap.get(beanName))
                .get();
    }
}
