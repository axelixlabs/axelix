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

import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConfigurationPropertiesBeanSerializer} implementations.
 *
 * @author Nikita Kirillov
 */
class ConfigurationPropertiesBeanSerializerTest {

    @ParameterizedTest
    @MethodSource("serializers")
    void shouldPreserveNullValues(ConfigurationPropertiesBeanSerializer serializer) {
        // when.
        Map<String, @Nullable Object> properties = serializer.serialize(new TestProperties(null));

        // then.
        assertThat(properties).containsEntry("unset", null);
    }

    static Stream<ConfigurationPropertiesBeanSerializer> serializers() {
        return Stream.of(
                new JacksonConfigurationPropertiesBeanSerializer(),
                new Jackson2ConfigurationPropertiesBeanSerializer());
    }

    record TestProperties(@Nullable String unset) {}
}
