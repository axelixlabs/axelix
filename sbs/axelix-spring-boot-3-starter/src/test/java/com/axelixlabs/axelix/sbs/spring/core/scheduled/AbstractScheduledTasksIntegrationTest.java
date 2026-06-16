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

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

/**
 * Base class for scheduled-task integration tests that should share a single Spring
 * {@link org.springframework.context.ApplicationContext}.
 *
 * <p>The shared context is keyed by the merged context configuration of this class. Because
 * Spring's TestContext framework merges inherited class-level annotations, every concrete
 * subclass that adds no further context configuration produces an identical cache key and
 * therefore reuses the very same cached context. The {@link AxelixScheduledTasksEndpointTest}
 * deliberately does <em>not</em> extend this class: it runs on a real web server with its own
 * property source and security configuration, so it keeps a separate context.
 *
 * @since 16.06.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
@SpringBootTest
@Import(AbstractScheduledTasksIntegrationTest.SharedScheduledTasksTestConfiguration.class)
abstract class AbstractScheduledTasksIntegrationTest {

    static volatile boolean cronFlag = false;

    static volatile boolean fixedDelayFlag = false;

    static volatile boolean fixedRateFlag = false;

    static volatile boolean fixedRateFlagForExecute = false;

    static volatile boolean customTaskFlag = false;

    @TestConfiguration
    @EnableScheduling
    static class SharedScheduledTasksTestConfiguration implements SchedulingConfigurer {

        static final String CUSTOM_TASK_ID =
                SharedScheduledTasksTestConfiguration.class.getName() + ".testCustomTask";

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

        // Custom
        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
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
            @Nullable
            public Instant nextExecution(@NonNull TriggerContext triggerContext) {
                return Instant.now().plusMillis(100);
            }
        }
    }
}
