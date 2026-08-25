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

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.CompressionMode;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.OtlpProperties.AXELIX_MASTER_METRICS_OTLP_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OtlpTransportMetricsAutoConfiguration}.
 *
 * @author Aleksei Ermakov
 * @author Dmitry Mazurov
 * @author Mikhail Polivakha
 */
class OtlpTransportMetricsAutoConfigurationTest {

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(OtlpTransportMetricsAutoConfigurationTest::isolatedContext)
                .withBean(Clock.class, () -> Clock.SYSTEM)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, OtlpTransportMetricsAutoConfiguration.class));
    }

    @Nested
    class WhenOtlpDisabled {

        @Test // GH-1496
        void shouldNotCreateAnyOtlpBeansWhenExplicitlyDisabled() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=false");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).doesNotHaveBean(OtlpConfig.class);
                assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
            });
        }

        @Test // GH-1496
        void shouldNotCreateAnyOtlpBeansWhenPropertyAbsent() {
            // given.
            ApplicationContextRunner contextRunner = baselineContextRunner();

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).doesNotHaveBean(OtlpConfig.class);
                assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class);
            });
        }
    }

    @Nested
    class WhenOtlpEnabled {

        @Test // GH-1496
        void shouldWireOtlpConfigAndMeterRegistry() {
            // given. // The defaults live in application.yaml, which the slice runner does not load, so the
            // values the registry needs to start (url/step/compression-mode) are supplied explicitly here.
            ApplicationContextRunner contextRunner = baselineContextRunner()
                    .withPropertyValues(
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=true",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".url=http://localhost:4318/v1/metrics",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".step=1m",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".compression-mode=none");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context).hasSingleBean(OtlpConfig.class);
                assertThat(context).hasSingleBean(OtlpMeterRegistry.class);
            });
        }

        @Test // GH-1496
        void shouldFailToStartWhenUrlIsMissing() {
            // given.
            ApplicationContextRunner contextRunner =
                    baselineContextRunner().withPropertyValues(AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=true");

            // when.
            contextRunner.run(context -> {
                // then.
                assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining(AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled");
            });
        }

        @Test // GH-1496
        void shouldMapAxelixPropertiesOntoOtlpConfig() {
            // given.
            ApplicationContextRunner contextRunner = baselineContextRunner()
                    .withPropertyValues(
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=true",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".url=http://collector:4318/v1/metrics",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".step=30s",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".headers.authorization=Bearer token",
                            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".compression-mode=gzip");

            // when.
            contextRunner.run(context -> {
                // then.
                OtlpConfig config = context.getBean(OtlpConfig.class);
                assertThat(config.url()).isEqualTo("http://collector:4318/v1/metrics");
                assertThat(config.step()).isEqualTo(Duration.ofSeconds(30));
                assertThat(config.headers()).containsEntry("authorization", "Bearer token");
                assertThat(config.compressionMode()).isEqualTo(CompressionMode.GZIP);
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
