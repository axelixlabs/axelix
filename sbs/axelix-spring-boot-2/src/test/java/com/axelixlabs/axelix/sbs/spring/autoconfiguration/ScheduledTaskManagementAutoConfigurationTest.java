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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.DefaultScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.IntervalBasedTaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskService;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksRegistry;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TriggerBasedTaskRescheduler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskManagementAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class ScheduledTaskManagementAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
            .withUserConfiguration(EnableSchedulingConfig.class)
            .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
            assertThat(context).hasSingleBean(ScheduledTaskService.class);
            assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
            assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);

            assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
            assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
            assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        new ApplicationContextRunner()
                .withUserConfiguration(EnableSchedulingConfig.class)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ScheduledTaskManagementAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ScheduledTasksRegistry.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskService.class);
                    assertThat(context).doesNotHaveBean(AxelixScheduledTasksEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfiguration_whenEndpointDisabled() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ScheduledTaskManagementAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ScheduledTasksRegistry.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskService.class);
                    assertThat(context).doesNotHaveBean(AxelixScheduledTasksEndpoint.class);
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomAxelixScheduledTasksEndpointConfig.class,
                        CustomScheduledTasksAssemblerConfig.class,
                        CustomScheduledTasksRegistryConfig.class)
                .run(context -> {
                    assertThat(context.getBean(ScheduledTasksRegistry.class))
                            .isExactlyInstanceOf(CustomScheduledTasksRegistry.class);
                    assertThat(context.getBean(ScheduledTasksAssembler.class))
                            .isExactlyInstanceOf(CustomScheduledTasksAssembler.class);
                    assertThat(context.getBean(AxelixScheduledTasksEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixScheduledTasksEndpoint.class);
                });
    }

    @TestConfiguration
    @EnableScheduling
    static class EnableSchedulingConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ThreadPoolTaskScheduler();
        }
    }

    @TestConfiguration
    static class CustomScheduledTasksRegistryConfig {
        @Bean
        public ScheduledTasksRegistry scheduledTasksRegistry(ObjectProvider<ScheduledTaskHolder> taskHolders) {
            return new CustomScheduledTasksRegistry(taskHolders.orderedStream().collect(Collectors.toList()));
        }
    }

    @TestConfiguration
    static class CustomScheduledTasksAssemblerConfig {
        @Bean
        public ScheduledTasksAssembler scheduledTasksAssembler(ScheduledTasksRegistry registry) {
            return new CustomScheduledTasksAssembler(registry);
        }
    }

    @TestConfiguration
    static class CustomAxelixScheduledTasksEndpointConfig {
        @Bean
        public AxelixScheduledTasksEndpoint axelixScheduledTasksEndpoint(
                ScheduledTaskService service, ScheduledTasksAssembler assembler) {
            return new CustomAxelixScheduledTasksEndpoint(service, assembler);
        }
    }

    static class CustomScheduledTasksRegistry extends ScheduledTasksRegistry {
        public CustomScheduledTasksRegistry(List<ScheduledTaskHolder> taskHolders) {
            super(taskHolders);
        }
    }

    static class CustomScheduledTasksAssembler extends DefaultScheduledTasksAssembler {
        public CustomScheduledTasksAssembler(ScheduledTasksRegistry registry) {
            super(registry);
        }
    }

    static class CustomAxelixScheduledTasksEndpoint extends AxelixScheduledTasksEndpoint {
        public CustomAxelixScheduledTasksEndpoint(ScheduledTaskService service, ScheduledTasksAssembler assembler) {
            super(service, assembler);
        }
    }
}
