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

import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.util.TestSocketUtils;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.ConditionalOnPrometheusPort.Mode;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.PROMETHEUS_PORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.SERVER_PORT_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OnPrometheusPortCondition}.
 *
 * @author Dmitry Mazurov
 */
class OnPrometheusPortConditionTest {

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(OnPrometheusPortConditionTest::isolatedContext)
                .withUserConfiguration(MarkerConfiguration.class);
    }

    @ParameterizedTest // GH-1520
    @MethodSource("matchingPortCases")
    void shouldActivateMatchesApplicationPortModeWhenPortsMatch(
            @Nullable String serverPort, @Nullable String prometheusPort) {
        // given.
        ApplicationContextRunner contextRunner =
                baselineContextRunner().withPropertyValues(properties(serverPort, prometheusPort));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(MatchesApplicationPortMarker.class);
            assertThat(context).doesNotHaveBean(CustomPortConfiguredMarker.class);
        });
    }

    @ParameterizedTest // GH-1520
    @MethodSource("differingPortCases")
    void shouldActivateCustomPortConfiguredModeWhenPortsDiffer(
            @Nullable String serverPort, @Nullable String prometheusPort) {
        // given.
        ApplicationContextRunner contextRunner =
                baselineContextRunner().withPropertyValues(properties(serverPort, prometheusPort));

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).doesNotHaveBean(MatchesApplicationPortMarker.class);
            assertThat(context).hasSingleBean(CustomPortConfiguredMarker.class);
        });
    }

    private static Stream<Arguments> matchingPortCases() {
        return Stream.of(Arguments.of("8080", null), Arguments.of("8080", "8080"), Arguments.of(null, "8080"));
    }

    private static Stream<Arguments> differingPortCases() {
        return Stream.of(Arguments.of("8080", String.valueOf(TestSocketUtils.findAvailableTcpPort())));
    }

    private static String[] properties(@Nullable String serverPort, @Nullable String prometheusPort) {
        return Stream.of(property(SERVER_PORT_PROPERTY, serverPort), property(PROMETHEUS_PORT_PROPERTY, prometheusPort))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    private static @Nullable String property(String name, @Nullable String value) {
        return value == null ? null : name + "=" + value;
    }

    private static @NonNull AnnotationConfigApplicationContext isolatedContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        StandardEnvironment cleanEnv = new StandardEnvironment();
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        context.setEnvironment(cleanEnv);
        return context;
    }

    @Configuration
    static class MarkerConfiguration {

        @Bean
        @ConditionalOnPrometheusPort(Mode.MATCHES_APPLICATION_PORT)
        MatchesApplicationPortMarker matchesApplicationPortMarker() {
            return new MatchesApplicationPortMarker();
        }

        @Bean
        @ConditionalOnPrometheusPort(Mode.CUSTOM_PORT_CONFIGURED)
        CustomPortConfiguredMarker customPortConfiguredMarker() {
            return new CustomPortConfiguredMarker();
        }
    }

    private record MatchesApplicationPortMarker() {}

    private record CustomPortConfiguredMarker() {}
}
