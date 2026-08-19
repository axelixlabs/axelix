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
package com.axelixlabs.axelix.master.autoconfiguration.metrics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.PROMETHEUS_PORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.SERVER_PORT_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OnMatchingPrometheusPortCondition}.
 *
 * @author Dmitry Mazurov
 */
class OnMatchingPrometheusPortConditionTest {

    @ParameterizedTest // GH-1520
    @CsvSource({
        "8080, , true, true",
        "8080, , false, false",
        "8080, 8080, true, true",
        "8080, 8080, false, false",
        "8080, 9404, true, false",
        "8080, 9404, false, true",
        "0, 0, true, false",
        "0, 0, false, true",
    })
    void evaluatesOutcomeAgainstExpectedMatchAttribute(
            String serverPort, String prometheusPort, boolean expectedMatch, boolean expectedOutcome) {
        // given.
        MockEnvironment environment = new MockEnvironment().withProperty(SERVER_PORT_PROPERTY, serverPort);
        if (prometheusPort != null) {
            environment.withProperty(PROMETHEUS_PORT_PROPERTY, prometheusPort);
        }

        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);

        Class<?> annotatedMarker = expectedMatch ? MatchesTrueMarker.class : MatchesFalseMarker.class;
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotations()).thenReturn(MergedAnnotations.from(annotatedMarker));

        OnMatchingPrometheusPortCondition condition = new OnMatchingPrometheusPortCondition();

        // when.
        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        // then.
        assertThat(outcome.isMatch()).isEqualTo(expectedOutcome);
    }

    @ConditionalOnMatchingPrometheusPort
    private static final class MatchesTrueMarker {}

    @ConditionalOnMatchingPrometheusPort(matches = false)
    private static final class MatchesFalseMarker {}
}
