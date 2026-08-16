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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.MetricsAutoConfiguration.PROMETHEUS_METRICS_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MetricsAutoConfiguration}.
 *
 * @author Dmitry Mazurov
 */
class MetricsAutoConfigurationTest {

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(MetricsAutoConfigurationTest::isolatedContext)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, MetricsAutoConfiguration.class));
    }

    @Test
    void shouldNotCreateCommonTagsCustomizerWhenPrometheusDisabled() {
        // given.
        ApplicationContextRunner contextRunner =
                baselineContextRunner().withPropertyValues(PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=false");

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).doesNotHaveBean(MeterRegistryCustomizer.class);
        });
    }

    @Test
    void shouldApplyConfiguredCommonTagsWhenPrometheusEnabled() {
        // given.
        ApplicationContextRunner contextRunner = baselineContextRunner()
                .withPropertyValues(
                        PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".enabled=true",
                        PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".tags.region=eu-west-1");

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasSingleBean(MeterRegistryCustomizer.class);

            @SuppressWarnings("unchecked")
            MeterRegistryCustomizer<MeterRegistry> customizer = context.getBean(MeterRegistryCustomizer.class);

            MeterRegistry registry = new SimpleMeterRegistry();
            customizer.customize(registry);
            registry.counter("test.counter").increment();

            assertThat(registry.find("test.counter").tags("region", "eu-west-1").counter())
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
            // then.
            assertThat(context).hasSingleBean(MeterRegistryCustomizer.class);

            @SuppressWarnings("unchecked")
            MeterRegistryCustomizer<MeterRegistry> customizer = context.getBean(MeterRegistryCustomizer.class);

            MeterRegistry registry = new SimpleMeterRegistry();
            customizer.customize(registry);
            registry.counter("test.counter").increment();

            assertThat(registry.get("test.counter").counter().getId().getTags()).isEmpty();
        });
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
