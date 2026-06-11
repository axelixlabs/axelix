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

import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Base class for the schedule-related integration tests.
 *
 * <p>Every beans test extends this class and declares no context-affecting annotations of its own
 * (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all the schedule tests produce an identical
 * merged context configuration and therefore share a single cached Spring application context,
 * which is only started once for the whole test run.
 *
 * <p>{@link AxelixScheduledTasksEndpointTest} is intentionally not part of this hierarchy.
 *
 * <p>The annotations below are the union of the configuration previously declared by the
 * individual schedule tests.
 */
@SpringBootTest
@Import({AbstractScheduledIntegrationTest.ScheduledTestConfiguration.class})
public abstract class AbstractScheduledIntegrationTest {
    public static volatile boolean customTaskFlag = false;

    public static volatile boolean cronFlag = false;

    public static volatile boolean fixedDelayFlag = false;

    public static volatile boolean fixedRateFlag = false;

    public static volatile boolean fixedRateFlagForExecute = false;

    public static final String CUSTOM_TASK_ID =
        ScheduledTestConfiguration.class.getName() + ".testCustomTask";

    public static final String CRON_TASK_ID = ScheduledTestConfiguration.class.getName() + ".testCronTask";

    public static final String CRON_TASK_ID_FOR_MODIFY =
        ScheduledTestConfiguration.class.getName() + ".testCronTaskForModify";

    public static final String FIXED_DELAY_TASK_ID =
        ScheduledTestConfiguration.class.getName() + ".testFixedDelayTask";

    public static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
        ScheduledTestConfiguration.class.getName() + ".testFixedDelayTaskForModify";

    public static final String FIXED_RATE_TASK_ID =
        ScheduledTestConfiguration.class.getName() + ".testFixedRateTask";

    public static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
        ScheduledTestConfiguration.class.getName() + ".testFixedRateTaskForModify";

    public static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
        ScheduledTestConfiguration.class.getName() + ".testFixedDelayTaskForExecute";

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTestConfiguration implements SchedulingConfigurer {
        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Bean
        public ScheduledTasksRegistry scheduledTaskRegistry(ScheduledAnnotationBeanPostProcessor processor) {
            return new ScheduledTasksRegistry(List.of(processor));
        }

        @Bean
        TaskRescheduler testTriggerBasedTaskRescheduler(TaskScheduler taskScheduler) {
            return new TriggerBasedTaskRescheduler(taskScheduler);
        }

        @Bean
        TaskRescheduler testIntervalBasedTaskRescheduler(TaskScheduler taskScheduler) {
            return new IntervalBasedTaskRescheduler(taskScheduler);
        }

        @Bean
        public ScheduledTaskService scheduledTaskService(
            ScheduledTasksRegistry registry,
            List<TaskRescheduler> taskReschedulers,
            ThreadPoolTaskExecutor taskExecutor) {
            return new ScheduledTaskService(registry, taskReschedulers, taskExecutor);
        }

        // Cron
        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

        @Scheduled(cron = "*/2 * * * * *")
        public void testCronTaskForModify() {}

        // FixedDelay
        @Scheduled(fixedDelay = 100)
        public void testFixedDelayTask() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedDelay = 200)
        public void testFixedDelayTaskForModify() {}

        // FixedRate
        @Scheduled(fixedRate = 100, initialDelay = 50)
        public void testFixedRateTask() {
            fixedRateFlag = true;
        }

        @Scheduled(fixedRate = 200)
        public void testFixedRateTaskForModify() {}

        @Scheduled(fixedRate = 2000000000)
        public void testFixedDelayTaskForExecute() {
            fixedRateFlagForExecute = true;
        }

        @Override
        public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
        }

        static class CustomTestTask implements Runnable {
            @Override
            public void run() {
                customTaskFlag = true;
            }

            @Override
            public String toString() {
                return CUSTOM_TASK_ID;
            }
        }

        static class CustomTestTrigger implements Trigger {
            @Override
            public Date nextExecutionTime(@NonNull TriggerContext triggerContext) {
                return Date.from(Instant.now().plusMillis(100));
            }
        }
    }
}
