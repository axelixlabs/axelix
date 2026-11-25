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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.contract.env.Property;
import com.axelixlabs.axelix.common.contract.env.PropertySource;
import com.axelixlabs.axelix.master.api.external.response.PropertyFindingResponse;
import com.axelixlabs.axelix.master.api.external.response.env.EnvironmentFeedResponse;
import com.axelixlabs.axelix.master.domain.findings.AxelixFinding;
import com.axelixlabs.axelix.master.domain.findings.AxelixFinding.FindingId;
import com.axelixlabs.axelix.master.domain.findings.FindingConcern;
import com.axelixlabs.axelix.master.domain.findings.FindingTechnology;
import com.axelixlabs.axelix.master.domain.findings.PropertyFinding;
import com.axelixlabs.axelix.master.service.transport.PropertySanitizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link EnvironmentFeedConverter}.
 *
 * @author Sergey Cherkasov
 */
class EnvironmentFeedConverterTest {

    private final Map<Property, List<PropertyFinding>> dangerousProperties = new IdentityHashMap<>();
    private final PropertySanitizer sanitizer = mock(PropertySanitizer.class);
    private final EnvironmentFeedConverter subject =
            new EnvironmentFeedConverter(feed -> dangerousProperties, sanitizer);

    @Test
    void shouldSetAllFindingsOnlyForDetectedOccurrence() {
        // given.
        EnvironmentFeed feed = openInViewFeedDetectedInFirstSource();

        // when.
        EnvironmentFeedResponse response = subject.convertNonNull(feed);

        // then.
        verify(sanitizer).sanitize(same(feed));
        assertThat(response.propertySources().get(0).properties().get(0).findings())
                .extracting(PropertyFindingResponse::findingId)
                .containsExactly("AXLX-1000", "AXLX-1019");
        assertThat(response.propertySources().get(1).properties().get(0).findings())
                .isEmpty();
    }

    @Test
    void shouldSerializeFindingsAndPreserveLegacyDangerousValue() {
        // given.
        EnvironmentFeed responseSource = openInViewFeedDetectedInFirstSource();

        // when.
        JsonNode response = new JsonMapper().valueToTree(subject.convertNonNull(responseSource));

        // then.
        JsonNode matched = response.at("/propertySources/0/properties/0");
        assertThat(matched.path("findings").size()).isEqualTo(2);
        assertThat(matched.at("/dangerousValue/findingId").asString()).isEqualTo("AXLX-1000");
        JsonNode overridden = response.at("/propertySources/1/properties/0");
        assertThat(overridden.path("findings").isArray()).isTrue();
        assertThat(overridden.path("findings").size()).isZero();
        assertThat(overridden.has("dangerousValue")).isFalse();
    }

    private EnvironmentFeed openInViewFeedDetectedInFirstSource() {
        Property effective =
                new Property().name("spring.jpa.open-in-view").value("true").isPrimary(true);
        Property overridden =
                new Property().name("spring.jpa.open-in-view").value("true").isPrimary(false);
        dangerousProperties.put(
                effective,
                List.of(
                        new PropertyFinding(
                                new AxelixFinding(
                                        new FindingId("AXLX-1000"),
                                        FindingConcern.PERFORMANCE,
                                        FindingTechnology.CONFIGURATION),
                                "Open Session in View is enabled.",
                                "false"),
                        new PropertyFinding(
                                new AxelixFinding(
                                        new FindingId("AXLX-1019"),
                                        FindingConcern.SECURITY,
                                        FindingTechnology.CONFIGURATION),
                                "A second finding for the same property.",
                                "false")));

        return new EnvironmentFeed()
                .activeProfiles(List.of("production"))
                .defaultProfiles(List.of("default"))
                .propertySources(List.of(
                        new PropertySource().name("first").properties(List.of(effective)),
                        new PropertySource().name("second").properties(List.of(overridden))));
    }
}
