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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.registry.otlp.CompressionMode;
import io.micrometer.registry.otlp.OtlpConfig;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.AxelixOtlpMetricsEnvironmentPostProcessor.AXELIX_MASTER_METRICS_OTLP_PREFIX;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.AxelixOtlpMetricsEnvironmentPostProcessor.MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.OtlpTransportMetricsAutoConfiguration.PROMETHEUS_METRICS_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link OtlpTransportMetricsAutoConfiguration}.
 *
 * @author Dmitry Mazurov
 * @author Aleksei Ermakov
 */
class OtlpTransportMetricsAutoConfigurationTest {

    private final AxelixOtlpMetricsEnvironmentPostProcessor postProcessor =
            new AxelixOtlpMetricsEnvironmentPostProcessor();

    private final SpringApplication application = new SpringApplication();

    private static ApplicationContextRunner baselineContextRunner() {
        return new ApplicationContextRunner(OtlpTransportMetricsAutoConfigurationTest::isolatedContext)
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, OtlpTransportMetricsAutoConfiguration.class));
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

    @Test // GH-1496
    void shouldNotCreateOtlpConfigWhenExportDisabled() {
        // given.
        ApplicationContextRunner contextRunner =
                baselineContextRunner().withPropertyValues(AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=false");

        // when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).doesNotHaveBean(OtlpConfig.class);
        });
    }

    @Test // GH-1496
    void shouldCreateOtlpConfigFromAxelixPropertiesWhenExportEnabled() {
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
            assertThat(context).hasSingleBean(OtlpConfig.class);
            OtlpConfig config = context.getBean(OtlpConfig.class);
            assertThat(config.url()).isEqualTo("http://collector:4318/v1/metrics");
            assertThat(config.step()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.headers()).containsEntry("authorization", "Bearer token");
            assertThat(config.compressionMode()).isEqualTo(CompressionMode.GZIP);
        });
    }

    @Test // GH-1496
    void shouldRunOtlpAdapterAfterConfigDataEnvironmentPostProcessor() {
        // when. // then.
        assertThat(postProcessor.getOrder())
                .isEqualTo(Ordered.LOWEST_PRECEDENCE)
                .isGreaterThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test // GH-1496
    void shouldDisableBootOtlpExportByDefault() {
        // given.
        MockEnvironment environment =
                new MockEnvironment().withProperty(MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX + ".enabled", "true");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX + ".enabled", Boolean.class))
                .isFalse();
    }

    @Test // GH-1496
    void shouldEnableBootOtlpExportFromAxelixProperty() {
        // given.
        MockEnvironment environment = new MockEnvironment()
                .withProperty(AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled", "true")
                .withProperty(MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX + ".enabled", "false");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX + ".enabled", Boolean.class))
                .isTrue();
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
