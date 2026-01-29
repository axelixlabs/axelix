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
package com.axelixlabs.axelix.sbs.spring.configprops;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.core.env.MapPropertySource;

import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link SmartSanitizingFunction}.
 *
 * @author Mikhail Polivakha
 */
class SmartSanitizingFunctionTest {

    private static final List<String> TO_BE_SANITIZED = List.of("SCREAMING_SNAKE", "dot.based", "camelCaseBased");

    private SmartSanitizingFunction subject;
    private PropertyNameNormalizer propertyNameNormalizer;

    @BeforeEach
    void setUp() {
        propertyNameNormalizer = new DefaultPropertyNameNormalizer();
    }

    @ParameterizedTest
    @MethodSource(value = "commonCaseArgs")
    void shouldSanitizeValue_CommonPath(SanitizableData input, Object expectedValue) {
        // given.
        subject = new SmartSanitizingFunction(TO_BE_SANITIZED, propertyNameNormalizer);
        Object beforeSanitization = input.getValue();

        // when.
        SanitizableData result = subject.apply(input);

        // then.
        assertThat(result.getPropertySource()).isSameAs(input.getPropertySource());
        assertThat(result.getKey()).isSameAs(input.getKey());
        assertThat(result.getValue()).isEqualTo(expectedValue);

        // we also should not chane the value in hte incoming SanitizableData
        assertThat(input.getValue()).isSameAs(beforeSanitization);
    }

    @ParameterizedTest
    @MethodSource(value = "sanitizeAllArgs")
    void shouldSanitizeValue_SanitizeAll(SanitizableData input) {
        // given.
        subject = new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
        Object beforeSanitization = input.getValue();

        // when.
        SanitizableData result = subject.apply(input);

        // then.
        assertThat(result.getPropertySource()).isSameAs(input.getPropertySource());
        assertThat(result.getKey()).isSameAs(input.getKey());
        assertThat(result.getValue()).isEqualTo("******");

        // we also should not chane the value in hte incoming SanitizableData
        assertThat(input.getValue()).isSameAs(beforeSanitization);
    }

    public static Stream<Arguments> commonCaseArgs() {
        var propertySource = new MapPropertySource("testPropertySource", Map.of());

        Stream<Arguments> exactMatch = TO_BE_SANITIZED.stream().map(it -> {
            return of(new SanitizableData(propertySource, it, "doesNotMatter"), "******");
        });

        Stream<Arguments> normalizationAppliedMatch = Stream.of(
                of(new SanitizableData(propertySource, "screaming.snake", "doesNotMatter"), "******"),
                of(new SanitizableData(propertySource, "DOT_BASED", "doesNotMatter"), "******"),
                of(new SanitizableData(propertySource, "camel.case.based", "doesNotMatter"), "******"));

        Stream<Arguments> shouldBeLeftAsIs =
                Stream.of(of(new SanitizableData(propertySource, "should.be.left.as.is", "itMatters"), "itMatters"));

        return Stream.of(exactMatch, normalizationAppliedMatch, shouldBeLeftAsIs)
                .flatMap(it -> it);
    }

    public static Stream<Arguments> sanitizeAllArgs() {
        var propertySource = new MapPropertySource("testPropertySource", Map.of());

        return Stream.of(
                of(new SanitizableData(propertySource, "simple", "doesNotMatter")),
                of(new SanitizableData(propertySource, "two.parts", "doesNotMatter")),
                of(new SanitizableData(propertySource, "camelCase", "doesNotMatter")),
                of(new SanitizableData(propertySource, "SNAKE_CASE", "doesNotMatter")));
    }
}
