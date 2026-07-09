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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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
    void shouldActivateWithoutReschedulers_whenSchedulingNotEnabled() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                .run(context -> {
                    // read path is available even without @EnableScheduling
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);

                    // no scheduling -> reschedulers are not created
                    assertThat(context).getBeans(TaskRescheduler.class).isEmpty();
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
}
