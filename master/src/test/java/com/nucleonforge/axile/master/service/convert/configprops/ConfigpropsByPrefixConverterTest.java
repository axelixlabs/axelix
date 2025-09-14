package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsByPrefixResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsProfile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigpropsByPrefixConverter}.
 *
 * @author Sergey Cherkasov
 */
public class ConfigpropsByPrefixConverterTest {
    private final ConfigpropsByPrefixConverter subject = new ConfigpropsByPrefixConverter();

    @Test
    @SuppressWarnings("unchecked")
    void testConvertHappyPath() {
        // when.
        ConfigpropsByPrefixResponse response = subject.convertInternal(new ConfigpropsFeed(Map.of(
                "application1",
                new ConfigpropsFeed.Context(beansMapContext1(), "parentId"),
                "application2",
                new ConfigpropsFeed.Context(beansMapContext2(), "parentId"))));

        // beans
        String beanName = "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties";
        List<ConfigpropsProfile> beans = response.beans().stream()
                .filter(b -> b.beanName().equals(beanName))
                .sorted(Comparator.comparingInt(b -> b.properties().size()))
                .toList();

        // application1 -> bean1
        ConfigpropsProfile bean1 = beans.get(1);
        assertThat(bean1.beanName()).isEqualTo(beanName);

        // application1 -> bean1 -> prefix
        assertThat(bean1.prefix()).isEqualTo("spring.jackson");

        // application1 -> bean1 ->  properties
        assertThat(bean1.properties().get("serialization1")).isEqualTo(Map.of("INDENT_OUTPUT", true));
        assertThat(bean1.properties().get("defaultPropertyInclusion1")).isEqualTo("NON_NULL");
        assertThat(bean1.properties().get("visibility1")).isEqualTo(Map.of());
        assertThat(bean1.properties().get("parser1")).isEqualTo(Map.of());
        assertThat(bean1.properties().get("deserialization1")).isEqualTo(Map.of());
        assertThat(bean1.properties().get("generator1")).isEqualTo(Map.of());
        assertThat(bean1.properties().get("mapper1")).isEqualTo(Map.of());

        // application1 -> bean1 -> inputs
        assertThat(bean1.inputs().get("visibility1")).isEqualTo(Map.of());
        assertThat(bean1.inputs().get("parser1")).isEqualTo(Map.of());
        assertThat(bean1.inputs().get("deserialization1")).isEqualTo(Map.of());
        assertThat(bean1.inputs().get("generator1")).isEqualTo(Map.of());
        assertThat(bean1.inputs().get("mapper1")).isEqualTo(Map.of());

        // application1 -> bean1 -> inputs -> "serialization"
        Map<String, Object> beans1InputsSerialization =
                (Map<String, Object>) bean1.inputs().get("serialization1");
        assertThat(beans1InputsSerialization)
                .containsEntry(
                        "INDENT_OUTPUT",
                        Map.of(
                                "value", "true",
                                "origin",
                                        "\"spring.jackson.serialization.indent_output\" from property source \"Inlined Test Properties\""));

        // application1 -> bean1 -> inputs -> "defaultPropertyInclusion"
        Map<String, Object> Beans1InputsDefaultPropertyInclusion =
                (Map<String, Object>) bean1.inputs().get("defaultPropertyInclusion1");
        assertThat(Beans1InputsDefaultPropertyInclusion).containsEntry("value", "non_null");
        assertThat(Beans1InputsDefaultPropertyInclusion)
                .containsEntry(
                        "origin",
                        "\"spring.jackson.default-property-inclusion\" from property source \"Inlined Test Properties\"");

        // application2 -> bean2
        ConfigpropsProfile bean2 = beans.get(0);
        assertThat(bean2.beanName()).isEqualTo(beanName);

        // application2 -> bean2 -> prefix
        assertThat(bean2.prefix()).isEqualTo("spring.jackson");

        // application2 -> bean2 ->  properties
        assertThat(bean2.properties().get("serialization2")).isEqualTo(Map.of("INDENT_OUTPUT", false));
        assertThat(bean2.properties().get("defaultPropertyInclusion2")).isEqualTo("NON_NULL");

        // application2 -> bean2 -> inputs -> "serialization"
        Map<String, Object> bean2InputsSerialization =
                (Map<String, Object>) bean2.inputs().get("serialization2");
        assertThat(bean2InputsSerialization)
                .containsEntry("INDENT_OUTPUT", Map.of("value", "true", "origin", Map.of()));

        // application2 -> bean2 -> inputs -> "defaultPropertyInclusion"
        Map<String, Object> bean2InputsDefaultPropertyInclusion =
                (Map<String, Object>) bean2.inputs().get("defaultPropertyInclusion2");
        assertThat(bean2InputsDefaultPropertyInclusion).containsEntry("value", "non_null");
        assertThat(bean2InputsDefaultPropertyInclusion).containsEntry("origin", Map.of());
    }

    private static Map<String, ConfigpropsFeed.Bean> beansMapContext1() {
        Map<String, Object> properties = Map.of(
                "serialization1", Map.of("INDENT_OUTPUT", true),
                "defaultPropertyInclusion1", "NON_NULL",
                "visibility1", Map.of(),
                "parser1", Map.of(),
                "deserialization1", Map.of(),
                "generator1", Map.of(),
                "mapper1", Map.of());

        Map<String, Object> inputs = Map.of(
                "serialization1",
                        Map.of(
                                "INDENT_OUTPUT",
                                Map.of(
                                        "value", "true",
                                        "origin",
                                                "\"spring.jackson.serialization.indent_output\" from property source \"Inlined Test Properties\"")),
                "defaultPropertyInclusion1",
                        Map.of(
                                "value", "non_null",
                                "origin",
                                        "\"spring.jackson.default-property-inclusion\" from property source \"Inlined Test Properties\""),
                "visibility1", Map.of(),
                "parser1", Map.of(),
                "deserialization1", Map.of(),
                "generator1", Map.of(),
                "mapper1", Map.of());

        return Map.of(
                "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties",
                new ConfigpropsFeed.Bean("spring.jackson", properties, inputs));
    }

    private static Map<String, ConfigpropsFeed.Bean> beansMapContext2() {
        Map<String, Object> properties =
                Map.of("serialization2", Map.of("INDENT_OUTPUT", false), "defaultPropertyInclusion2", "NON_NULL");

        Map<String, Object> inputs = Map.of(
                "serialization2", Map.of("INDENT_OUTPUT", Map.of("value", "true", "origin", Map.of())),
                "defaultPropertyInclusion2", Map.of("value", "non_null", "origin", Map.of()));

        return Map.of(
                "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties",
                new ConfigpropsFeed.Bean("spring.jackson", properties, inputs));
    }
}
