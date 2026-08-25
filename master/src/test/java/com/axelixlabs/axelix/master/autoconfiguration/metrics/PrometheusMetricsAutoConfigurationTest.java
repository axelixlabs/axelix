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

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PrometheusMetricsAutoConfiguration}.
 *
 * @author Dmitry Mazurov
 * @author Mikhail Polivakha
 */
class PrometheusMetricsAutoConfigurationTest {

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(PrometheusMetricsAutoConfigurationTest::isolatedContext)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, PrometheusMetricsAutoConfiguration.class));
    }

    @Nested
    class WhenPrometheusDisabled {

        @Test
        void shouldNotCreateAnyPrometheusBeansWhenExplicitlyDisabled() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=false");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).doesNotHaveBean(PrometheusMeterRegistry.class);
                assertThat(context).doesNotHaveBean(PrometheusScrapeEndpoint.class);
                assertThat(context).doesNotHaveBean(PrometheusRegistry.class);
            });
        }

        @Test
        void shouldNotCreateAnyPrometheusBeansWhenPropertyAbsent() {
            // given.
            ApplicationContextRunner contextRunner = baselineContextRunner();

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).doesNotHaveBean(PrometheusMeterRegistry.class);
                assertThat(context).doesNotHaveBean(PrometheusScrapeEndpoint.class);
                assertThat(context).doesNotHaveBean(PrometheusRegistry.class);
            });
        }
    }

    @Nested
    class WhenPrometheusEnabled {

        @Test
        void shouldWireTheFullPrometheusBeanGraph() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=true");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(PrometheusConfig.class);
                assertThat(context).hasSingleBean(PrometheusRegistry.class);
                assertThat(context).hasSingleBean(Clock.class);
                assertThat(context).hasSingleBean(PrometheusMeterRegistry.class);
                assertThat(context).hasSingleBean(PrometheusScrapeEndpoint.class);
            });
        }

        @Test
        void shouldExposeMetersRegisteredViaMeterRegistryThroughSharedPrometheusRegistry() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=true");

            // when.
            contextRunner.run(context -> {
                PrometheusMeterRegistry meterRegistry = context.getBean(PrometheusMeterRegistry.class);
                PrometheusRegistry prometheusRegistry = context.getBean(PrometheusRegistry.class);

                meterRegistry.counter("axelix.test.counter").increment();

                // then.
                MetricSnapshots snapshots = prometheusRegistry.scrape();
                assertThat(snapshots)
                        .anyMatch(snapshot -> "axelix_test_counter"
                                .equals(snapshot.getMetadata().getName()));
            });
        }

        @Test
        void shouldApplyConfiguredCommonTags() {
            // given.
            ApplicationContextRunner contextRunner = baselineContextRunner()
                    .withPropertyValues(
                            PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=true",
                            PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".tags.region=eu-west-1");

            // when.
            contextRunner.run(context -> {
                PrometheusMeterRegistry registry = context.getBean(PrometheusMeterRegistry.class);
                registry.counter("test.counter").increment();

                // then.
                assertThat(registry.find("test.counter")
                                .tags("region", "eu-west-1")
                                .counter())
                        .isNotNull();
            });
        }

        @Test
        void shouldNotAddAnyTagsWhenNoTagsConfigured() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=true");

            // when.
            contextRunner.run(context -> {
                PrometheusMeterRegistry registry = context.getBean(PrometheusMeterRegistry.class);
                registry.counter("test.counter").increment();

                // then.
                assertThat(registry.get("test.counter").counter().getId().getTags())
                        .isEmpty();
            });
        }
    }

    private static @NonNull AnnotationConfigApplicationContext isolatedContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        StandardEnvironment cleanEnv = new StandardEnvironment();
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        cleanEnv.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        context.setEnvironment(cleanEnv);
        return context;
    }
}
