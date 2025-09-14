package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
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
    @SuppressWarnings("unchecked")
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
        assertThat(beanProfile1.properties()).isNotEmpty().isNotNull();
        assertThat(beanProfile1.properties().get("allowedOrigins")).isEqualTo(List.of());
        assertThat(beanProfile1.properties().get("maxAge")).isEqualTo("PT30M");
        assertThat(beanProfile1.properties().get("exposedHeaders")).isEqualTo(List.of());
        assertThat(beanProfile1.properties().get("allowedOriginPatterns")).isEqualTo(List.of());
        assertThat(beanProfile1.properties().get("allowedHeaders")).isEqualTo(List.of());
        assertThat(beanProfile1.properties().get("allowedMethods")).isEqualTo(List.of());

        // bean1 -> inputs
        assertThat(beanProfile1.inputs()).isNotNull();
        assertThat(beanProfile1.inputs().get("allowedOrigins")).isEqualTo(List.of());
        assertThat(beanProfile1.inputs().get("maxAge")).isEqualTo(Map.of());
        assertThat(beanProfile1.inputs().get("exposedHeaders")).isEqualTo(List.of());
        assertThat(beanProfile1.inputs().get("allowedOriginPatterns")).isEqualTo(List.of());
        assertThat(beanProfile1.inputs().get("allowedHeaders")).isEqualTo(List.of());
        assertThat(beanProfile1.inputs().get("allowedMethods")).isEqualTo(List.of());

        // bean2
        ConfigpropsProfile beanProfile2 = getBeanByName(configpropsFeedResponse, "bean2");
        assertThat(beanProfile2.beanName()).isEqualTo("bean2");

        // bean2 -> prefix
        assertThat(beanProfile2.prefix()).isEqualTo("management.endpoints.web");

        // bean2 -> properties
        assertThat(beanProfile2.properties().get("pathMapping")).isEqualTo(Map.of());
        assertThat(beanProfile2.properties().get("basePath")).isEqualTo("/actuator");
        assertThat(beanProfile2.properties().get("discovery")).isEqualTo(Map.of("enabled", true));

        // bean2 -> properties -> "exposure"
        Map<String, Object> exposureProperties =
                (Map<String, Object>) beanProfile2.properties().get("exposure");
        assertThat(exposureProperties).containsEntry("include", List.of("*"));
        assertThat(exposureProperties).containsEntry("exclude", List.of());

        // bean2 -> inputs
        assertThat(beanProfile2.inputs().get("pathMapping")).isEqualTo(Map.of());
        assertThat(beanProfile2.inputs().get("basePath")).isEqualTo(Map.of());
        assertThat(beanProfile2.inputs().get("discovery")).isEqualTo(Map.of("enabled", Map.of()));

        // bean2 -> inputs -> "exposure"
        Map<String, Object> exposureInputs =
                (Map<String, Object>) beanProfile2.inputs().get("exposure");
        assertThat(exposureInputs)
                .containsEntry(
                        "include",
                        List.of(
                                Map.of(
                                        "value",
                                        "*",
                                        "origin",
                                        "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\"")));
        assertThat(exposureInputs).containsEntry("exclude", List.of());

        // bean3
        ConfigpropsProfile beanProfile3 = getBeanByName(configpropsFeedResponse, "bean3");
        assertThat(beanProfile3.beanName()).isEqualTo("bean3");

        // application2 -> bean2 -> prefix
        assertThat(beanProfile3.prefix()).isEqualTo("spring.jackson");

        // application2 -> bean2 ->  properties
        assertThat(beanProfile3.properties().get("serialization2")).isEqualTo(Map.of("INDENT_OUTPUT", false));
        assertThat(beanProfile3.properties().get("defaultPropertyInclusion2")).isEqualTo("NON_NULL");

        // application2 -> bean2 -> inputs -> "serialization"
        Map<String, Object> bean3InputsSerialization =
                (Map<String, Object>) beanProfile3.inputs().get("serialization2");
        assertThat(bean3InputsSerialization)
                .containsEntry("INDENT_OUTPUT", Map.of("value", "true", "origin", Map.of()));

        // application2 -> bean2 -> inputs -> "defaultPropertyInclusion"
        Map<String, Object> bean3InputsDefaultPropertyInclusion =
                (Map<String, Object>) beanProfile3.inputs().get("defaultPropertyInclusion2");
        assertThat(bean3InputsDefaultPropertyInclusion).containsEntry("value", "non_null");
        assertThat(bean3InputsDefaultPropertyInclusion).containsEntry("origin", Map.of());
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
