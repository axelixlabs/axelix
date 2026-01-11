/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.configprops;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.core.env.MapPropertySource;

import com.nucleonforge.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;

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
        subject = new SmartSanitizingFunction(ConfigPropsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
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
