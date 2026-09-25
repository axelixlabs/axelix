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
package com.axelixlabs.axelix.master.service.env;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.master.api.external.response.env.DangerousProperty;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.contract.env.Property;
import com.axelixlabs.axelix.master.contract.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultDangerousPropertyDetector}.
 *
 * @author Sergey Cherkasov
 */
class DefaultDangerousPropertyDetectorTest {

    private final DefaultDangerousPropertyDetector subject =
            new DefaultDangerousPropertyDetector(new DefaultPropertyNameNormalizer());

    @Test
    void shouldDetectDangerousValueInHighestPrioritySource() {
        // given.
        Property property = property("spring.jpa.open-in-view", "true", true);
        EnvironmentFeed feed = feed(propertySource(property));

        // when.
        Map<Property, DangerousProperty> result = subject.detect(feed);

        // then.
        assertThat(result).containsExactly(Map.entry(property, DangerousProperty.OPEN_IN_VIEW));
    }

    @Test
    void shouldDetectOnlyFirstOccurrenceOfEqualProperties() {
        // given.
        Property effective = property("spring.jpa.open-in-view", "true", true);
        Property overridden = property("spring.jpa.open-in-view", "true", false);
        EnvironmentFeed feed = feed(propertySource(effective), propertySource(overridden));

        // when.
        Map<Property, DangerousProperty> result = subject.detect(feed);

        // then.
        assertThat(result).hasSize(1);
        assertThat(result.keySet().iterator().next()).isSameAs(effective);
    }

    @Test
    void shouldNotInspectSanitizedValue() {
        // given.
        EnvironmentFeed feed = feed(
                propertySource(property("spring.jpa.open-in-view", "******", true)),
                propertySource(property("spring.jpa.open-in-view", "true", false)));

        // when.
        Map<Property, DangerousProperty> result = subject.detect(feed);

        // then.
        assertThat(result).isEmpty();
    }

    @Test
    void shouldTreatDifferentSpellingsAsSameProperty() {
        // given.
        EnvironmentFeed feed = feed(
                propertySource(property("SPRING_JPA_OPEN_IN_VIEW", "false", true)),
                propertySource(property("spring.jpa.open-in-view", "true", false)));

        // when.
        Map<Property, DangerousProperty> result = subject.detect(feed);

        // then.
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotDetectNonDangerousValue() {
        // given.
        EnvironmentFeed feed = feed(propertySource(property("spring.jpa.open-in-view", "false", true)));

        // when.
        Map<Property, DangerousProperty> result = subject.detect(feed);

        // then.
        assertThat(result).isEmpty();
    }

    private EnvironmentFeed feed(PropertySource... propertySources) {
        return new EnvironmentFeed()
                .activeProfiles(List.of())
                .defaultProfiles(List.of())
                .propertySources(List.of(propertySources));
    }

    private PropertySource propertySource(Property property) {
        return new PropertySource().name("source").properties(List.of(property));
    }

    private Property property(String name, String value, boolean isPrimary) {
        return new Property().name(name).value(value).isPrimary(isPrimary);
    }
}
