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
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
 * Test configuration for {@link AxelixScheduledTasksEndpointTest}, part of the shared endpoint
 * test context.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@TestConfiguration
@EnableScheduling
public class ScheduledTasksEndpointTestConfiguration implements SchedulingConfigurer {

    static final String CUSTOM_TASK_ID = CustomTestTask.class.getName();

    static final String CUSTOM_TRIGGER = "CustomTestTrigger";

    static final String NON_EXISTENT_TASK_ID = "com.example.NonExistentTask.doWork";

    static volatile boolean cronFlag = false;

    static volatile boolean fixedDelayFlag = false;

    static volatile boolean fixedRateFlag = false;

    static volatile boolean customTaskFlag = false;

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

    @Bean
    public ScheduledTasksAssembler serviceScheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
    }

    @Bean
    public AxelixScheduledTasksEndpoint scheduledTasksEndpointExtension(
            ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
        return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
    }

    // Cron tasks
    @Scheduled(cron = "*/1 * * * * *")
    public void testCronTask() {
        cronFlag = true;
    }

    @Scheduled(cron = "*/2 * * * * *")
    public void testCronTaskForModify() {}

    // FixedDelay tasks
    @Scheduled(fixedDelay = 100)
    public void testFixedDelayTask() {
        fixedDelayFlag = true;
    }

    @Scheduled(fixedDelay = 20000000)
    public void testFixedDelayTaskForModify() {}

    @Scheduled(fixedDelay = 2000000000)
    public void testFixedDelayTaskForExecute() {
        fixedDelayFlag = true;
    }

    // FixedRate tasks
    @Scheduled(fixedRate = 100, initialDelay = 50)
    public void testFixedRateTask() {
        fixedRateFlag = true;
    }

    @Scheduled(fixedRate = 20000000)
    public void testFixedRateTaskForModify() {}

    @Scheduled(fixedRate = 2000000000)
    public void testFixedRateTaskForExecute() {
        fixedRateFlag = true;
    }

    // Custom task
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
        public Date nextExecutionTime(@NonNull TriggerContext triggerContext) {
            return Date.from(Instant.now().plusMillis(100));
        }

        @Override
        public String toString() {
            return CUSTOM_TRIGGER;
        }
    }
}
