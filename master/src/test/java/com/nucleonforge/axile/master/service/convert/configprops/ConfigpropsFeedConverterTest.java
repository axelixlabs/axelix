package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.KeyValue;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsFeedResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsProfile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigpropsFeedConverter}.
 *
 * @author Sergey Cherkasov
 */
public class ConfigpropsFeedConverterTest {

    private final ConfigpropsFeedConverter subject = new ConfigpropsFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        ConfigpropsFeedResponse configpropsFeedResponse = subject.convertInternal(new ConfigpropsFeed(Map.of(
                "application1",
                new ConfigpropsFeed.Context(beansMapContext1(), "parentId"),
                "application2",
                new ConfigpropsFeed.Context(beansMapContext2(), "parentId"))));

        // bean1
        ConfigpropsProfile beanProfile1 = getBeanByName(configpropsFeedResponse, "bean1");
        assertThat(beanProfile1.beanName()).isEqualTo("bean1");

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
        ConfigpropsProfile beanProfile2 = getBeanByName(configpropsFeedResponse, "bean2");
        assertThat(beanProfile2.beanName()).isEqualTo("bean2");

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
        ConfigpropsProfile beanProfile3 = getBeanByName(configpropsFeedResponse, "bean3");
        assertThat(beanProfile3.beanName()).isEqualTo("bean3");

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

    private static ConfigpropsProfile getBeanByName(ConfigpropsFeedResponse configpropsFeedResponse, String beanName) {
        return configpropsFeedResponse.beans().stream()
                .filter(profile -> profile.beanName().equals(beanName))
                .findFirst()
                .get();
    }

    private static Map<String, ConfigpropsFeed.Bean> beansMapContext1() {
        // bean1 -> properties
        Map<String, Object> bean1Properties = Map.of(
                "allowedOrigins", List.of(),
                "maxAge", "PT30M",
                "exposedHeaders", List.of(),
                "allowedOriginPatterns", List.of(),
                "allowedHeaders", List.of(),
                "allowedMethods", List.of());

        // bean1 -> inputs
        Map<String, Object> bean1Inputs = Map.of(
                "allowedOrigins", List.of(),
                "maxAge", Map.of(),
                "exposedHeaders", List.of(),
                "allowedOriginPatterns", List.of(),
                "allowedHeaders", List.of(),
                "allowedMethods", List.of());

        // bean2 -> properties
        Map<String, Object> bean2Properties = Map.of(
                "pathMapping", Map.of(),
                "exposure", Map.of("include", List.of("*"), "exclude", List.of()),
                "basePath", "/actuator",
                "discovery", Map.of("enabled", true));

        // bean2 -> inputs
        Map<String, Object> bean2Inputs = Map.of(
                "pathMapping", Map.of(),
                "exposure",
                        Map.of(
                                "include",
                                List.of(
                                        Map.of(
                                                "value",
                                                "*",
                                                "origin",
                                                "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\"")),
                                "exclude",
                                List.of()),
                "basePath", Map.of(),
                "discovery", Map.of("enabled", Map.of()));

        // return
        return Map.of(
                "bean1",
                new ConfigpropsFeed.Bean("management.endpoints.web.cors", bean1Properties, bean1Inputs),
                "bean2",
                new ConfigpropsFeed.Bean("management.endpoints.web", bean2Properties, bean2Inputs));
    }

    private static Map<String, ConfigpropsFeed.Bean> beansMapContext2() {
        Map<String, Object> properties =
                Map.of("serialization2", Map.of("INDENT_OUTPUT", false), "defaultPropertyInclusion2", "NON_NULL");

        Map<String, Object> inputs = Map.of(
                "serialization2", Map.of("INDENT_OUTPUT", Map.of("value", "true", "origin", Map.of())),
                "defaultPropertyInclusion2", Map.of("value", "non_null", "origin", Map.of()));

        return Map.of("bean3", new ConfigpropsFeed.Bean("spring.jackson", properties, inputs));
    }
}
