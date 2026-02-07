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
package com.axelixlabs.axelix.master.service.convert;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.env.EnvironmentProperty;
import com.axelixlabs.axelix.common.api.env.PropertyValue;
import com.axelixlabs.axelix.master.api.external.response.EnvironmentPropertyResponse;
import com.axelixlabs.axelix.master.service.convert.response.environment.EnvironmentPropertyConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentPropertyConverter}
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
class EnvironmentPropertyConverterTest {

    private final EnvironmentPropertyConverter subject = new EnvironmentPropertyConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        EnvironmentPropertyResponse response = subject.convertInternal(getEnvironmentProperty());

        // then.
        assertThat(response.source()).isEqualTo("systemProperties");
        assertThat(response.value()).isEqualTo("Amazon.com Inc.");

        assertThat(response.propertySources())
                .hasSize(2)
                .extracting(EnvironmentPropertyResponse.PropertySource::name)
                .containsExactly("systemProperties", "systemEnvironment");

        EnvironmentPropertyResponse.PropertySource systemPropertiesSource =
                response.propertySources().get(0);
        assertThat(systemPropertiesSource.property()).isNotNull().satisfies(pv -> {
            assertThat(pv.value()).isEqualTo("Amazon.com Inc.");
            assertThat(pv.origin()).isNull();
        });

        EnvironmentPropertyResponse.PropertySource systemEnvironmentSource =
                response.propertySources().get(1);
        assertThat(systemEnvironmentSource.property()).isNotNull().satisfies(pv -> {
            assertThat(pv.value()).isEqualTo("Amazon");
            assertThat(pv.origin()).isNull();
        });
    }

    private EnvironmentProperty getEnvironmentProperty() {
        EnvironmentProperty.Property property = new EnvironmentProperty.Property("systemProperties", "Amazon.com Inc.");

        List<String> activeProfiles = new ArrayList<>();
        activeProfiles.add("production");

        List<String> defaultProfiles = new ArrayList<>();
        defaultProfiles.add("default");
        defaultProfiles.add("development");

        List<EnvironmentProperty.SourceEntry> sourceEntries = new ArrayList<>();
        sourceEntries.add(new EnvironmentProperty.SourceEntry("server.ports", null));
        sourceEntries.add(new EnvironmentProperty.SourceEntry("servletConfigInitParams", null));
        sourceEntries.add(
                new EnvironmentProperty.SourceEntry("systemProperties", new PropertyValue("Amazon.com Inc.", null)));
        sourceEntries.add(new EnvironmentProperty.SourceEntry("systemEnvironment", new PropertyValue("Amazon", null)));

        return new EnvironmentProperty(property, activeProfiles, defaultProfiles, sourceEntries);
    }
}
