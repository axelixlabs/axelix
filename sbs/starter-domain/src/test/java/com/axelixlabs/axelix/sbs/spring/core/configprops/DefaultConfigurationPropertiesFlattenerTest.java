/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultConfigurationPropertiesFlattener}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
class DefaultConfigurationPropertiesFlattenerTest {

    private final ConfigurationPropertiesFlattener flattener = new DefaultConfigurationPropertiesFlattener();

    @ParameterizedTest
    @MethodSource("propertiesArgs")
    void shouldFlatten(Map<String, Object> properties, List<ConfigurationPropertiesEntry> expected) {

        // when.
        List<ConfigurationPropertiesEntry> result = flattener.flatten(properties);

        // then.
        assertThat(result).containsExactlyElementsOf(expected);
    }

    private static Stream<Arguments> propertiesArgs() {
        Map<String, Object> nestedProperties = linkedMap(
                "axelix",
                linkedMap(
                        "app",
                        linkedMap(
                                "name",
                                "axelix",
                                "server",
                                linkedMap("host", "localhost", "ports", List.of(8080, 8081)))));

        Map<String, Object> deepProperties = linkedMap(
                "axelix",
                linkedMap(
                        "app",
                        linkedMap(
                                "server",
                                linkedMap(
                                        "ports",
                                        List.of(linkedMap(
                                                "name", linkedMap("url", List.of(linkedMap("schema", "https")))))))));

        return Stream.of(
                Arguments.of(
                        linkedMap(
                                "unset",
                                null,
                                "literal",
                                "null",
                                "nested",
                                linkedMap("value", null),
                                "items",
                                Arrays.asList(null, "null")),
                        List.of(
                                new ConfigurationPropertiesEntry().key("unset"),
                                new ConfigurationPropertiesEntry()
                                        .key("literal")
                                        .value("null"),
                                new ConfigurationPropertiesEntry().key("nested.value"),
                                new ConfigurationPropertiesEntry().key("items[0]"),
                                new ConfigurationPropertiesEntry()
                                        .key("items[1]")
                                        .value("null"))),
                Arguments.of(
                        Map.of("originParents", new String[] {"parent", "ancestor"}),
                        List.of(
                                new ConfigurationPropertiesEntry()
                                        .key("originParents[0]")
                                        .value("parent"),
                                new ConfigurationPropertiesEntry()
                                        .key("originParents[1]")
                                        .value("ancestor"))),
                Arguments.of(
                        linkedMap("map", Map.of(), "list", List.of(), "array", new String[0]),
                        List.of(
                                new ConfigurationPropertiesEntry().key("map"),
                                new ConfigurationPropertiesEntry().key("list"),
                                new ConfigurationPropertiesEntry().key("array"))),
                Arguments.of((Map<String, Object>) null, List.of()),
                Arguments.of(Map.of(), List.of()),
                Arguments.of(
                        nestedProperties,
                        List.of(
                                new ConfigurationPropertiesEntry()
                                        .key("axelix.app.name")
                                        .value("axelix"),
                                new ConfigurationPropertiesEntry()
                                        .key("axelix.app.server.host")
                                        .value("localhost"),
                                new ConfigurationPropertiesEntry()
                                        .key("axelix.app.server.ports[0]")
                                        .value("8080"),
                                new ConfigurationPropertiesEntry()
                                        .key("axelix.app.server.ports[1]")
                                        .value("8081"))),
                Arguments.of(
                        deepProperties,
                        List.of(new ConfigurationPropertiesEntry()
                                .key("axelix.app.server.ports[0].name.url[0].schema")
                                .value("https"))));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> linkedMap(Object... entries) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }
}
