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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void convertsSanitizedPropertiesAndInputsToResponse() {
        // given.
        ConfigurationPropertiesEntry property =
                new ConfigurationPropertiesEntry().key("secret").value("original");
        ConfigurationPropertiesEntry input =
                new ConfigurationPropertiesEntry().key("secret.value").value("original");
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("client")
                        .prefix("app")
                        .properties(List.of(property, new ConfigurationPropertiesEntry().key("empty")))
                        .inputs(List.of(
                                input,
                                new ConfigurationPropertiesEntry()
                                        .key("secret.origin")
                                        .value("config.yml")))));
        PropertySanitizer sanitizer = mock(PropertySanitizer.class);
        doAnswer(invocation -> {
                    property.setValue("sanitized");
                    input.setValue("sanitized");
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
                                new ConfigurationPropertiesFeedResponse.Entry("secret", "sanitized", null),
                                new ConfigurationPropertiesFeedResponse.Entry("empty", null, null)),
                        List.of(
                                new ConfigurationPropertiesFeedResponse.Entry("secret.value", "sanitized", null),
                                new ConfigurationPropertiesFeedResponse.Entry("secret.origin", "config.yml", null))));
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

    @ParameterizedTest
    @CsvSource({
        "spring.jpa, openInView, true, true",
        "spring.jpa, openInView, , true",
        "spring.jpa, open-in-view, false, false",
        "'', spring.jpa.showSql, TRUE, true",
        "management.endpoints.web, exposure.include[2], *, true"
    })
    void detectsDangerousBoundValues(String prefix, String key, String value, boolean dangerous) {
        // given.
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("config")
                        .prefix(prefix)
                        .properties(List.of(
                                new ConfigurationPropertiesEntry().key(key).value(value)))
                        .inputs(List.of(new ConfigurationPropertiesEntry()
                                .key(key + ".value")
                                .value(value)))));
        ConfigurationPropertiesFeedConverter subject = new ConfigurationPropertiesFeedConverter(
                mock(PropertySanitizer.class), new DefaultPropertyNameNormalizer());

        // when.
        ConfigurationPropertiesFeedResponse response = subject.convertNonNull(feed);

        // then.
        ConfigurationPropertiesFeedResponse.Bean bean = response.beans().get(0);
        if (dangerous) {
            assertThat(bean.properties().get(0).dangerousValue().rationale()).isNotBlank();
            assertThat(bean.properties().get(0).dangerousValue().alternativeExample())
                    .isNotBlank();
        } else {
            assertThat(bean.properties().get(0).dangerousValue()).isNull();
        }
        assertThat(bean.inputs().get(0).dangerousValue()).isNull();
    }

    @Test
    void doesNotDiscloseDangerousSanitizedValue() {
        // given.
        ConfigurationPropertiesEntry property =
                new ConfigurationPropertiesEntry().key("showSql").value("true").sensitive(true);
        ConfigurationPropertiesFeed feed = new ConfigurationPropertiesFeed()
                .beans(List.of(new ConfigurationPropertiesFeedBean()
                        .beanName("jpa")
                        .prefix("spring.jpa")
                        .properties(List.of(property))));
        PropertySanitizer sanitizer = mock(PropertySanitizer.class);
        doAnswer(_ -> {
                    property.setValue("******");
                    return null;
                })
                .when(sanitizer)
                .sanitize(feed);
        ConfigurationPropertiesFeedConverter subject =
                new ConfigurationPropertiesFeedConverter(sanitizer, new DefaultPropertyNameNormalizer());

        // when.
        ConfigurationPropertiesFeedResponse response = subject.convertNonNull(feed);

        // then.
        assertThat(response.beans().get(0).properties())
                .containsExactly(new ConfigurationPropertiesFeedResponse.Entry("showSql", "******", null));
    }
}
