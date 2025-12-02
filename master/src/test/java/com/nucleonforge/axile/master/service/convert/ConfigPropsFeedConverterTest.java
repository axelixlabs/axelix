package com.nucleonforge.axile.master.service.convert;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.master.api.response.ConfigPropsFeedResponse;
import com.nucleonforge.axile.master.service.convert.response.ConfigPropsFeedConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigPropsFeedConverter}.
 *
 * @author Sergey Cherkasov
 */
public class ConfigPropsFeedConverterTest {

    private final ConfigPropsFeedConverter subject = new ConfigPropsFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        ConfigPropsFeedResponse configPropsFeedResponse = subject.convertInternal(new ConfigPropsFeed(Map.of(
                "application1", new ConfigPropsFeed.Context(beansMapContext1(), "parentId1"),
                "application2", new ConfigPropsFeed.Context(beansMapContext2(), "parentId2"))));

        // bean1
        ConfigPropsFeedResponse.ConfigPropsProfile beanProfile1 =
                getBeanByName(configPropsFeedResponse, "org.springframework.boot.Bean1");

        assertThat(beanProfile1.beanName()).isEqualTo("org.springframework.boot.Bean1");

        // bean1 -> prefix
        assertThat(beanProfile1.prefix()).isEqualTo("management.endpoints.web.cors");

        // bean1 -> properties
        assertThat(beanProfile1.properties())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", "PT30M"),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean1 -> inputs
        assertThat(beanProfile1.inputs())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", null),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean2
        ConfigPropsFeedResponse.ConfigPropsProfile beanProfile2 =
                getBeanByName(configPropsFeedResponse, "org.springframework.boot.Bean2");
        assertThat(beanProfile2.beanName()).isEqualTo("org.springframework.boot.Bean2");

        // bean2 -> prefix
        assertThat(beanProfile2.prefix()).isEqualTo("management.endpoints.web");

        // bean2 -> properties
        assertThat(beanProfile2.properties())
                .containsOnly(
                        new KeyValue("pathMapping", null),
                        new KeyValue("basePath", "/actuator"),
                        new KeyValue("discovery.enabled", "true"),
                        new KeyValue("exposure.include[0]", "*"),
                        new KeyValue("exposure.exclude", null));

        // bean2 -> inputs
        assertThat(beanProfile2.inputs())
                .containsOnly(
                        new KeyValue("pathMapping", null),
                        new KeyValue("basePath", null),
                        new KeyValue("discovery.enabled", null),
                        new KeyValue("exposure.include[0].value", "*"),
                        new KeyValue(
                                "exposure.include[0].origin",
                                "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\""),
                        new KeyValue("exposure.exclude", null));

        // bean3
        ConfigPropsFeedResponse.ConfigPropsProfile beanProfile3 =
                getBeanByName(configPropsFeedResponse, "org.springframework.boot.Bean3");
        assertThat(beanProfile3.beanName()).isEqualTo("org.springframework.boot.Bean3");

        // application2 -> bean3 -> prefix
        assertThat(beanProfile3.prefix()).isEqualTo("spring.jackson");

        // application2 -> bean3 ->  properties
        assertThat(beanProfile3.properties())
                .containsOnly(
                        new KeyValue("serialization2.INDENT_OUTPUT", "false"),
                        new KeyValue("defaultPropertyInclusion2", "NON_NULL"));

        // application2 -> bean3 -> inputs
        assertThat(beanProfile3.inputs())
                .containsOnly(
                        new KeyValue("serialization2.INDENT_OUTPUT.value", "true"),
                        new KeyValue("serialization2.INDENT_OUTPUT.origin", null),
                        new KeyValue("defaultPropertyInclusion2.value", "non_null"),
                        new KeyValue("defaultPropertyInclusion2.origin", null));
    }

    private static ConfigPropsFeedResponse.ConfigPropsProfile getBeanByName(
            ConfigPropsFeedResponse configpropsFeedResponse, String beanName) {
        return configpropsFeedResponse.beans().stream()
                .filter(profile -> profile.beanName().equals(beanName))
                .findFirst()
                .get();
    }

    private static Map<String, ConfigPropsFeed.Bean> beansMapContext1() {
        // bean1 -> properties
        List<KeyValue> bean1Properties = List.of(
                new KeyValue("allowedOrigins", null),
                new KeyValue("maxAge", "PT30M"),
                new KeyValue("exposedHeaders", null),
                new KeyValue("allowedOriginPatterns", null),
                new KeyValue("allowedHeaders", null),
                new KeyValue("allowedMethods", null));

        // bean1 -> inputs
        List<KeyValue> bean1Inputs = List.of(
                new KeyValue("allowedOrigins", null),
                new KeyValue("maxAge", null),
                new KeyValue("exposedHeaders", null),
                new KeyValue("allowedOriginPatterns", null),
                new KeyValue("allowedHeaders", null),
                new KeyValue("allowedMethods", null));

        // bean2 -> properties
        List<KeyValue> bean2Properties = List.of(
                new KeyValue("pathMapping", null),
                new KeyValue("basePath", "/actuator"),
                new KeyValue("discovery.enabled", "true"),
                new KeyValue("exposure.include[0]", "*"),
                new KeyValue("exposure.exclude", null));

        // bean2 -> inputs
        List<KeyValue> bean2Inputs = List.of(
                new KeyValue("pathMapping", null),
                new KeyValue("basePath", null),
                new KeyValue("discovery.enabled", null),
                new KeyValue("exposure.include[0].value", "*"),
                new KeyValue(
                        "exposure.include[0].origin",
                        "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\""),
                new KeyValue("exposure.exclude", null));

        // return
        return Map.of(
                // bean1
                "org.springframework.boot.Bean1",
                new ConfigPropsFeed.Bean("management.endpoints.web.cors", bean1Properties, bean1Inputs),

                // bean2
                "org.springframework.boot.Bean2",
                new ConfigPropsFeed.Bean("management.endpoints.web", bean2Properties, bean2Inputs));
    }

    private static Map<String, ConfigPropsFeed.Bean> beansMapContext2() {
        // bean3 -> properties
        List<KeyValue> properties = List.of(
                new KeyValue("serialization2.INDENT_OUTPUT", "false"),
                new KeyValue("defaultPropertyInclusion2", "NON_NULL"));

        // bean3 -> inputs
        List<KeyValue> inputs = List.of(
                new KeyValue("serialization2.INDENT_OUTPUT.value", "true"),
                new KeyValue("serialization2.INDENT_OUTPUT.origin", null),
                new KeyValue("defaultPropertyInclusion2.value", "non_null"),
                new KeyValue("defaultPropertyInclusion2.origin", null));

        // bean3
        return Map.of("org.springframework.boot.Bean3", new ConfigPropsFeed.Bean("spring.jackson", properties, inputs));
    }
}
