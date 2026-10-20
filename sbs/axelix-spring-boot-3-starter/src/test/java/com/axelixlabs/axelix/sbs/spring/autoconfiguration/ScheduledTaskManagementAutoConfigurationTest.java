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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ScheduledTaskManagementAutoConfiguration.TaskManagementThreadPools;
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
 * @author Dmitry Mazurov
 */
class ScheduledTaskManagementAutoConfigurationTest {

    private static final String ENDPOINT_EXPOSURE_PROPERTY =
            "management.endpoints.web.exposure.include=axelix-scheduled-tasks";
    private static final String TASK_SCHEDULER_BEAN_NAME = "taskScheduler";
    private static final String USER_TASK_SCHEDULER_BEAN_NAME = "userTaskScheduler";
    private static final String TASK_EXECUTOR_FIELD = "taskExecutor";
    private static final String TASK_SCHEDULER_FIELD = "taskScheduler";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
            .withUserConfiguration(EnableSchedulingConfig.class)
            .withConfiguration(AutoConfigurations.of(
                    TaskSchedulingAutoConfiguration.class,
                    TaskExecutionAutoConfiguration.class,
                    ScheduledTaskManagementAutoConfiguration.class));

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

            // Axelix resources must not make the Spring Boot's own infrastructure beans back off
            assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
            assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
            assertThat(context).hasBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
            assertThat(context).hasBean(TASK_SCHEDULER_BEAN_NAME);
            assertThat(context).hasSingleBean(TaskManagementThreadPools.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        // given
        new ApplicationContextRunner()
                .withUserConfiguration(EnableSchedulingConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        TaskSchedulingAutoConfiguration.class,
                        TaskExecutionAutoConfiguration.class,
                        ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    assertThat(context).doesNotHaveBean(ScheduledTaskManagementAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ScheduledTasksRegistry.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskService.class);
                    assertThat(context).doesNotHaveBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).doesNotHaveBean(TaskManagementThreadPools.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context).hasBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
                    assertThat(context).hasBean(TASK_SCHEDULER_BEAN_NAME);
                });
    }

    @Test
    void shouldActivateReschedulers_backedByAxelixScheduler_whenSchedulingNotEnabled() {
        // given
        new ApplicationContextRunner()
                .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    // read path is available even without @EnableScheduling
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).hasSingleBean(TaskManagementThreadPools.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskScheduler.class);
                    assertThat(context)
                            .doesNotHaveBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
                    assertThat(context).doesNotHaveBean(TASK_SCHEDULER_BEAN_NAME);

                    // reschedulers are backed by the scheduler of TaskManagementThreadPools, so they are
                    // created even without @EnableScheduling
                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
                    assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
                    assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);
                });
    }

    @Test // GH-1485
    void shouldCreateAllBeans_whenThereAreOtherThreadPoolTaskExecutorsInContext() {
        // given: there are other ThreadPoolTaskExecutors in context

        contextRunner
                .withUserConfiguration(ThreadPoolTaskExecutorsConfig.class)
                // when
                .run(context -> {
                    // then
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).hasSingleBean(TaskManagementThreadPools.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context).hasBean(TASK_SCHEDULER_BEAN_NAME);
                    // Spring Boot backs off its applicationTaskExecutor when there are user-declared executors
                    assertThat(context)
                            .doesNotHaveBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);

                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
                    assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
                    assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);

                    // our executor is not a bean, so only the user-declared ones are in the context
                    // and the ambiguity between them does not affect the ScheduledTaskService
                    assertThat(context).getBeans(ThreadPoolTaskExecutor.class).hasSize(2);
                });
    }

    @Test // GH-1726
    void shouldUseOwnScheduler_whenUserDeclaresTaskScheduler() {
        // given: the user declares own TaskScheduler

        contextRunner
                .withUserConfiguration(ThreadPoolTaskSchedulerConfig.class)
                // when
                .run(context -> {
                    // then
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context).hasBean(USER_TASK_SCHEDULER_BEAN_NAME);
                    assertThat(context).doesNotHaveBean(TASK_SCHEDULER_BEAN_NAME);
                    assertThat(context).hasSingleBean(TaskManagementThreadPools.class);
                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);

                    ThreadPoolTaskScheduler userScheduler =
                            context.getBean(USER_TASK_SCHEDULER_BEAN_NAME, ThreadPoolTaskScheduler.class);
                    TaskManagementThreadPools resources = context.getBean(TaskManagementThreadPools.class);

                    assertThat(resources.scheduler()).isNotSameAs(userScheduler);
                });
    }

    @Test // GH-1726
    void shouldWireServiceAndReschedulersToTaskManagementThreadPools_whenThereAreOtherSchedulingBeansInContext() {
        // given: there are Spring Boot's and user-declared executors and scheduler in context

        contextRunner
                .withUserConfiguration(ThreadPoolTaskSchedulerConfig.class, ThreadPoolTaskExecutorsConfig.class)
                // when
                .run(context -> {
                    // then
                    TaskManagementThreadPools resources = context.getBean(TaskManagementThreadPools.class);

                    assertThat(context.getBean(ScheduledTaskService.class))
                            .extracting(TASK_EXECUTOR_FIELD)
                            .isSameAs(resources.executor());

                    assertThat(context.getBean(IntervalBasedTaskRescheduler.class))
                            .extracting(TASK_SCHEDULER_FIELD)
                            .isSameAs(resources.scheduler());
                    assertThat(context.getBean(TriggerBasedTaskRescheduler.class))
                            .extracting(TASK_SCHEDULER_FIELD)
                            .isSameAs(resources.scheduler());
                });
    }

    @Test // GH-1726
    void shouldRunTasksOnNamedThreads_inSchedulingResources() {
        contextRunner.run(context -> {
            TaskManagementThreadPools resources = context.getBean(TaskManagementThreadPools.class);

            String executorThreadName = resources
                    .executor()
                    .submit(() -> Thread.currentThread().getName())
                    .get(5, TimeUnit.SECONDS);
            String schedulerThreadName = resources
                    .scheduler()
                    .submit(() -> Thread.currentThread().getName())
                    .get(5, TimeUnit.SECONDS);

            assertThat(executorThreadName).startsWith(TaskManagementThreadPools.EXECUTOR_NAME);
            assertThat(schedulerThreadName).startsWith(TaskManagementThreadPools.SCHEDULER_NAME);
        });
    }

    @Test // GH-1726
    void shouldInitializeAndShutdownSchedulingResources_withContextLifecycle() {
        contextRunner.run(context -> {
            TaskManagementThreadPools resources = context.getBean(TaskManagementThreadPools.class);
            ThreadPoolTaskExecutor executor = resources.executor();
            ThreadPoolTaskScheduler scheduler = resources.scheduler();

            assertThat(executor.getThreadPoolExecutor().isShutdown()).isFalse();
            assertThat(scheduler.getScheduledExecutor().isShutdown()).isFalse();

            context.close();

            assertThat(executor.getThreadPoolExecutor().isShutdown()).isTrue();
            assertThat(scheduler.getScheduledExecutor().isShutdown()).isTrue();
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

        // simulation of schedule tasks
        @Scheduled(fixedDelay = 10000)
        public void someMockTask() {}
    }

    @TestConfiguration
    static class ThreadPoolTaskSchedulerConfig {

        @Bean
        public ThreadPoolTaskScheduler userTaskScheduler() {
            return new ThreadPoolTaskScheduler();
        }
    }

    @TestConfiguration
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
