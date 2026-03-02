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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry} with {@link TaskScheduler}.
 *
 * @author Aleksei Ermakov
 */
@SpringBootTest(
        classes = ScheduledTasksRegistryWithTaskSchedulerTest.ScheduledTaskRegistryWithTaskSchedulerConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ScheduledTasksRegistryWithTaskSchedulerTest {

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @Test
    void shouldWrapTasksWhenTaskSchedulerProvided() {
        // given.
        Collection<ManagedScheduledTask> tasks = taskRegistry.getAll();

        // when.
        Collection<Runnable> runnables =
                tasks.stream().map(ManagedScheduledTask::getRunnable).collect(Collectors.toList());

        // then.
        assertThat(tasks).isNotEmpty();
        assertThat(runnables).allSatisfy(runnable -> assertThat(runnable).isInstanceOf(TrackingRunnable.class));
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTaskRegistryWithTaskSchedulerConfiguration implements SchedulingConfigurer {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Bean
        public ScheduledTasksRegistry scheduledTaskRegistry(
                ScheduledAnnotationBeanPostProcessor processor, TaskScheduler taskScheduler) {
            return new ScheduledTasksRegistry(List.of(processor), taskScheduler);
        }

        @Scheduled(cron = "*/2 * * * * *")
        public void testCronTask() {}

        @Scheduled(fixedDelay = 2000)
        public void testFixedDelayTask() {}

        @Scheduled(fixedRate = 2000, initialDelay = 100)
        public void testFixedRateTask() {}

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTask(), new CustomTrigger());
        }

        private static class CustomTask implements Runnable {
            @Override
            public void run() {}
        }

        private static class CustomTrigger implements Trigger {
            @Override
            @Nullable
            public Instant nextExecution(@NonNull TriggerContext triggerContext) {
                return Instant.now().plusMillis(1000);
            }
        }
    }
}
