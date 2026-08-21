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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.IntervalBasedTaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ManagedScheduledTask;
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
 * @author Vyacheslav Yanin
 * @author Dmitry Kiselev
 */
class ScheduledTaskManagementAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
            .withUserConfiguration(EnableSchedulingConfig.class)
            .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        // given: ApplicationContextRunner configured with required properties and scheduling enabled

        // when
        contextRunner.run(context -> {
            // then
            assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
            assertThat(context).hasSingleBean(ScheduledTaskService.class);
            assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
            assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);

            assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
            assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
            assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);
            assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        // given
        new ApplicationContextRunner()
                .withUserConfiguration(EnableSchedulingConfig.class)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    assertThat(context).doesNotHaveBean(ScheduledTaskManagementAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ScheduledTasksRegistry.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskService.class);
                    assertThat(context).doesNotHaveBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor.class);
                });
    }

    @Test
    void shouldActivateReschedulers_backedByLocalScheduler_whenSchedulingNotEnabled() {
        // given
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    // read path is available even without @EnableScheduling
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);

                    // reschedulers are backed by the locally-declared TaskScheduler, so they are
                    // created even without @EnableScheduling
                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
                    assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
                    assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);
                });
    }

    @Test // GH-1485
    void shouldCreateAllBeans_whenThereAreOtherThreadPoolTaskExecutorsInContext() {
        // given: there are other ThreadPoolTaskExecutors in context

        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-scheduled-tasks")
                .withUserConfiguration(EnableSchedulingConfig.class)
                .withUserConfiguration(ThreadPoolTaskExecutorsConfig.class)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);

                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
                    assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
                    assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);

                    // our executor is registered alongside the user-declared ones, and the
                    // ScheduledTaskService is wired via @Qualifier despite the ambiguity
                    assertThat(context).getBeans(ThreadPoolTaskExecutor.class).hasSize(3);
                });
    }

    @Test // GH-1497
    void shouldCancelAllManagedTasksWhenContextCloses() {
        contextRunner.run(context -> {
            ScheduledTasksRegistry registry = context.getBean(ScheduledTasksRegistry.class);

            Collection<ManagedScheduledTask> snapshot = new ArrayList<>(registry.getAll());

            assertThat(snapshot).isNotEmpty();

            for (ManagedScheduledTask task : snapshot) {
                assertThat(task.isEnabled()).isTrue();
            }

            context.close();

            for (ManagedScheduledTask task : snapshot) {
                assertThat(task.isEnabled()).isFalse();
            }
        });
    }

    @TestConfiguration
    @EnableScheduling
    static class EnableSchedulingConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ThreadPoolTaskScheduler();
        }

        // simulation of schedule tasks
        @Scheduled(fixedDelay = 10000)
        public void someMockTask() {}
    }

    @TestConfiguration
    @EnableScheduling
    static class ThreadPoolTaskExecutorsConfig {

        @Bean
        public ThreadPoolTaskExecutor threadPoolTaskExecutor1() {
            return new ThreadPoolTaskExecutor();
        }

        @Bean
        public ThreadPoolTaskExecutor threadPoolTaskExecutor2() {
            return new ThreadPoolTaskExecutor();
        }
    }
}
