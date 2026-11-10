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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeedBean;
import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.contract.env.Property;
import com.axelixlabs.axelix.common.contract.env.PropertySource;
import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PropertySensitivityMarker}.
 *
 * @author Nikita Kirillov
 */
class PropertySensitivityMarkerTest {

    @ParameterizedTest
    @ValueSource(strings = {"app.client-secret", "APP_CLIENT_SECRET", "app.clientSecret"})
    void matchesNormalizedNames(String name) {
        // given.
        PropertySensitivityMarker subject =
                new PropertySensitivityMarker(List.of("app.client-secret"), new DefaultPropertyNameNormalizer());
        Property sensitive = new Property().name(name).value("secret").isPrimary(true);
        Property other = new Property().name("app.public").value("visible").isPrimary(true);
        EnvironmentFeed feed = new EnvironmentFeed()
                .propertySources(List.of(new PropertySource().name("test").properties(List.of(sensitive, other))));
        // when.
        subject.mark(feed);
        // then.
        assertThat(sensitive.getSensitive()).isTrue();
        assertThat(other.getSensitive()).isFalse();
    }

    @Test
    void marksEnvironmentWithoutChangingValues() {
        // given.
        PropertySensitivityMarker subject =
                new PropertySensitivityMarker(List.of("*"), new DefaultPropertyNameNormalizer());
        Property property = new Property().name("app.secret").value("original").isPrimary(true);
        EnvironmentFeed feed = new EnvironmentFeed()
                .propertySources(List.of(new PropertySource().name("test").properties(List.of(property))));
        // when.
        EnvironmentFeed result = subject.mark(feed);
        // then.
        assertThat(result.getPropertySources().get(0).getProperties().get(0).getSensitive())
                .isTrue();
        assertThat(property.getValue()).isEqualTo("original");
    }

    @Test
    void marksAllPropertiesWhenWildcardIsMixedWithNames() {
        // given.
        PropertySensitivityMarker subject =
                new PropertySensitivityMarker(List.of("app.named", "*"), new DefaultPropertyNameNormalizer());
        Property named = new Property().name("app.named").value("named").isPrimary(true);
        Property other = new Property().name("app.other").value("other").isPrimary(true);
        EnvironmentFeed feed = new EnvironmentFeed()
                .propertySources(List.of(new PropertySource().name("test").properties(List.of(named, other))));

        // when.
        subject.mark(feed);

        // then.
        assertThat(named.getSensitive()).isTrue();
        assertThat(other.getSensitive()).isTrue();
        assertThat(named.getValue()).isEqualTo("named");
        assertThat(other.getValue()).isEqualTo("other");
    }

    @Test
    void marksFullNamesAndBindingValuesButPreservesOrigins() {
        // given.
        PropertySensitivityMarker subject =
                new PropertySensitivityMarker(List.of("app.client.secret"), new DefaultPropertyNameNormalizer());
        ConfigurationPropertiesEntry secret =
                new ConfigurationPropertiesEntry().key("client.secret").value("original");
        ConfigurationPropertiesEntry input =
                new ConfigurationPropertiesEntry().key("client.secret.value").value("original");
        ConfigurationPropertiesEntry origin =
                new ConfigurationPropertiesEntry().key("client.secret.origin").value("file:config.yml");
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("test")
                        .prefix("app")
                        .properties(List.of(secret))
                        .inputs(List.of(input, origin))));
        // when.
        subject.mark(feed);
        // then.
        assertThat(secret.getSensitive()).isTrue();
        assertThat(input.getSensitive()).isTrue();
        assertThat(origin.getSensitive()).isFalse();
        assertThat(secret.getValue()).isEqualTo("original");
        assertThat(input.getValue()).isEqualTo("original");
    }
}
