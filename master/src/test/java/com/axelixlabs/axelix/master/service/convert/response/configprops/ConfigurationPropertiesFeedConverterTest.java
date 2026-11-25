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
package com.axelixlabs.axelix.master.service.convert.response.configprops;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeedBean;
import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.master.api.external.response.configprops.ConfigurationPropertiesFeedResponse;
import com.axelixlabs.axelix.master.service.transport.PropertySanitizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link ConfigurationPropertiesFeedConverter}.
 *
 * @author Nikita Kirillov
 */
class ConfigurationPropertiesFeedConverterTest {

    @Test
    void convertsSanitizedPropertiesToResponse() {
        // given.
        ConfigurationPropertiesEntry property =
                new ConfigurationPropertiesEntry().key("secret").value("original");
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("client")
                        .prefix("app")
                        .properties(List.of(property, new ConfigurationPropertiesEntry().key("empty")))));
        PropertySanitizer sanitizer = mock(PropertySanitizer.class);
        doAnswer(invocation -> {
                    property.setValue("sanitized");
                    return null;
                })
                .when(sanitizer)
                .sanitize(feed);
        ConfigurationPropertiesFeedConverter subject =
                new ConfigurationPropertiesFeedConverter(sanitizer, new DefaultPropertyNameNormalizer());

        // when.
        ConfigurationPropertiesFeedResponse response = subject.convertNonNull(feed);

        // then.
        assertThat(response.beans())
                .containsExactly(new ConfigurationPropertiesFeedResponse.Bean(
                        "client",
                        "app",
                        List.of(
                                new ConfigurationPropertiesFeedResponse.Entry("secret", "sanitized", List.of()),
                                new ConfigurationPropertiesFeedResponse.Entry("empty", null, List.of()))));
        verify(sanitizer).sanitize(same(feed));
        verifyNoMoreInteractions(sanitizer);
    }

    @Test
    void convertsEmptyFeed() {
        // given.
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed();
        PropertySanitizer sanitizer = mock(PropertySanitizer.class);
        ConfigurationPropertiesFeedConverter subject =
                new ConfigurationPropertiesFeedConverter(sanitizer, new DefaultPropertyNameNormalizer());

        // when.
        ConfigurationPropertiesFeedResponse response = subject.convertNonNull(feed);

        // then.
        assertThat(response.beans()).isEmpty();
        verify(sanitizer).sanitize(same(feed));
    }

    private static Stream<Arguments> dangerousBoundValues() {
        return Stream.of(
                Arguments.of("spring.jpa", "openInView", "true", "AXLX-1000"),
                Arguments.of("spring.jpa", "openInView", null, "AXLX-1000"),
                Arguments.of("spring.jpa", "open-in-view", "false", null),
                Arguments.of("", "spring.jpa.showSql", "TRUE", "AXLX-1004"),
                Arguments.of("management.endpoints.web", "exposure.include[2]", "*", "AXLX-1017"),
                Arguments.of("server.error", "include-message", "always", "AXLX-1009"),
                Arguments.of("spring.web.error", "include-message", "on-param", "AXLX-1009"));
    }

    @ParameterizedTest
    @MethodSource("dangerousBoundValues")
    void detectsDangerousBoundValues(String prefix, String key, String value, String expectedFindingId) {
        // given.
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("config")
                        .prefix(prefix)
                        .properties(List.of(
                                new ConfigurationPropertiesEntry().key(key).value(value)))));
        ConfigurationPropertiesFeedConverter subject = new ConfigurationPropertiesFeedConverter(
                mock(PropertySanitizer.class), new DefaultPropertyNameNormalizer());

        // when.
        ConfigurationPropertiesFeedResponse response = subject.convertNonNull(feed);

        // then.
        ConfigurationPropertiesFeedResponse.Bean bean = response.beans().get(0);
        var findings = bean.properties().get(0).findings();
        if (expectedFindingId == null) {
            assertThat(findings).isEmpty();
        } else {
            assertThat(findings).singleElement().satisfies(finding -> {
                assertThat(finding.findingId()).isEqualTo(expectedFindingId);
                assertThat(finding.rationale()).isNotBlank();
                assertThat(finding.alternativeExample()).isNotBlank();
            });
        }
    }
}
