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
package com.axelixlabs.axelix.master.service.convert.response.env;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.api.external.response.env.DangerousProperty;
import com.axelixlabs.axelix.master.api.external.response.env.EnvironmentFeedResponse;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.contract.env.Property;
import com.axelixlabs.axelix.master.contract.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EnvironmentFeedConverter}.
 *
 * @author Sergey Cherkasov
 */
class EnvironmentFeedConverterTest {

    private final Map<Property, DangerousProperty> dangerousProperties = new IdentityHashMap<>();
    private final EnvironmentFeedConverter subject = new EnvironmentFeedConverter(feed -> dangerousProperties);

    @Test
    void shouldSetDangerousValueOnlyForDetectedOccurrence() {
        // given.
        EnvironmentFeed feed = openInViewFeedDetectedInFirstSource();

        // when.
        EnvironmentFeedResponse response = subject.convertInternal(feed);

        // then.
        assertThat(response.propertySources().get(0).properties().get(0).dangerousValue())
                .isEqualTo(new EnvironmentFeedResponse.DangerousValue(
                        DangerousProperty.OPEN_IN_VIEW.getRationale(),
                        DangerousProperty.OPEN_IN_VIEW.getAlternativeExample()));
        assertThat(response.propertySources().get(1).properties().get(0).dangerousValue())
                .isNull();
    }

    private EnvironmentFeed openInViewFeedDetectedInFirstSource() {
        Property effective =
                new Property().name("spring.jpa.open-in-view").value("true").isPrimary(true);
        Property overridden =
                new Property().name("spring.jpa.open-in-view").value("true").isPrimary(false);
        dangerousProperties.put(effective, DangerousProperty.OPEN_IN_VIEW);

        return new EnvironmentFeed()
                .activeProfiles(List.of("production"))
                .defaultProfiles(List.of("default"))
                .propertySources(List.of(
                        new PropertySource().name("first").properties(List.of(effective)),
                        new PropertySource().name("second").properties(List.of(overridden))));
    }
}
