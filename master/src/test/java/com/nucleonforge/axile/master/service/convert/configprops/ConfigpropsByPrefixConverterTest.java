package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.KeyValue;
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
        assertThat(bean1.properties())
                .containsOnly(
                        new KeyValue("serialization1.INDENT_OUTPUT", "true"),
                        new KeyValue("defaultPropertyInclusion1", "NON_NULL"),
                        new KeyValue("visibility1", null),
                        new KeyValue("parser1", null),
                        new KeyValue("deserialization1", null),
                        new KeyValue("generator1", null),
                        new KeyValue("mapper1", null));

        // application1 -> bean1 -> inputs
        assertThat(bean1.inputs())
                .containsOnly(
                        new KeyValue("visibility1", null),
                        new KeyValue("parser1", null),
                        new KeyValue("deserialization1", null),
                        new KeyValue("generator1", null),
                        new KeyValue("mapper1", null),
                        new KeyValue("serialization1.INDENT_OUTPUT.value", "true"),
                        new KeyValue(
                                "serialization1.INDENT_OUTPUT.origin",
                                "\"spring.jackson.serialization.indent_output\" from property source \"Inlined Test Properties\""),
                        new KeyValue("defaultPropertyInclusion1.value", "non_null"),
                        new KeyValue(
                                "defaultPropertyInclusion1.origin",
                                "\"spring.jackson.default-property-inclusion\" from property source \"Inlined Test Properties\""));

        // application2 -> bean2
        ConfigpropsProfile bean2 = beans.get(0);
        assertThat(bean2.beanName()).isEqualTo(beanName);

        // application2 -> bean2 -> prefix
        assertThat(bean2.prefix()).isEqualTo("spring.jackson");

        // application2 -> bean2 ->  properties
        assertThat(bean2.properties())
                .containsOnly(
                        new KeyValue("serialization2.INDENT_OUTPUT", "false"),
                        new KeyValue("defaultPropertyInclusion2", "NON_NULL"));

        // application2 -> bean2 -> inputs
        assertThat(bean2.inputs())
                .containsOnly(
                        new KeyValue("serialization2.INDENT_OUTPUT.value", "true"),
                        new KeyValue("serialization2.INDENT_OUTPUT.origin", null),
                        new KeyValue("defaultPropertyInclusion2.value", "non_null"),
                        new KeyValue("defaultPropertyInclusion2.origin", null));
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
