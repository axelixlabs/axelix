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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksRegistryTest.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry}.
 *
 * <p>This class owns its own scheduled-task fixtures (see {@link Configuration} below) so it runs in its own
 * Spring {@code ApplicationContext}, isolated from the rest of the endpoint suite. The fixtures are deliberately
 * minimal: one task per Spring scheduling primitive (cron / fixedDelay / fixedRate / custom trigger), all on
 * long intervals so they don't tick noticeably during the test.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = Main.class)
@Import(Configuration.class)
class ScheduledTasksRegistryTest {

    private static final String CRON_TASK_ID = Configuration.class.getName() + ".testCronTask";
    private static final String FIXED_DELAY_TASK_ID = Configuration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID = Configuration.class.getName() + ".testFixedRateTask";
    private static final String CUSTOM_TASK_ID = Configuration.CustomTestTask.class.getName();

    private static final List<String> EXPECTED_TASK_IDS =
            List.of(CRON_TASK_ID, FIXED_DELAY_TASK_ID, FIXED_RATE_TASK_ID, CUSTOM_TASK_ID);

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @Test
    void shouldRegisterAllScheduledTasks() {
        Collection<ManagedScheduledTask> registeredTasks = taskRegistry.getAll();

        assertThat(registeredTasks).isNotNull().isNotEmpty();

        List<String> taskIds =
                registeredTasks.stream().map(ManagedScheduledTask::getId).collect(Collectors.toList());

        assertThat(taskIds)
                .allSatisfy(id -> assertThat(id).isNotBlank())
                .containsExactlyInAnyOrderElementsOf(EXPECTED_TASK_IDS);

        assertThat(registeredTasks)
                .allSatisfy(task -> assertThat(task.getFuture().isCancelled()).isFalse());

        EXPECTED_TASK_IDS.forEach(id -> assertThat(taskRegistry.find(id))
                .isPresent()
                .get()
                .extracting(ManagedScheduledTask::getId)
                .isEqualTo(id));

        assertThat(taskRegistry.find("non-existent-task")).isEmpty();
    }

    @Test
    void shouldHaveCorrectTaskTypes() {
        Collection<ManagedScheduledTask> tasks = taskRegistry.getAll();

        // Each of the four expected variants must produce its own concrete Spring {@link ScheduledTask} type.
        assertThat(findScheduledTask(tasks, CRON_TASK_ID).getTask()).isInstanceOf(CronTask.class);
        assertThat(findScheduledTask(tasks, FIXED_DELAY_TASK_ID).getTask()).isInstanceOf(FixedDelayTask.class);
        assertThat(findScheduledTask(tasks, FIXED_RATE_TASK_ID).getTask()).isInstanceOf(FixedRateTask.class);
        assertThat(findScheduledTask(tasks, CUSTOM_TASK_ID).getTask())
                .isInstanceOf(TriggerTask.class)
                .isNotInstanceOf(CronTask.class);
    }

    private static ScheduledTask findScheduledTask(Collection<ManagedScheduledTask> tasks, String taskId) {
        return tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .map(ManagedScheduledTask::getScheduledTask)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Minimal scheduling fixtures - one task per primitive, all on intervals long enough that they don't fire
     * during a typical test run. The test only inspects registry metadata, never observes a side-effect of a
     * tick.
     */
    @TestConfiguration
    static class Configuration implements SchedulingConfigurer {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Scheduled(cron = "0 0 0 1 1 ?")
        public void testCronTask() {
            // intentionally empty - the test inspects the registry, not the side effect.
        }

        @Scheduled(fixedDelay = 2_000_000_000L)
        public void testFixedDelayTask() {
            // intentionally empty
        }

        @Scheduled(fixedRate = 2_000_000_000L, initialDelay = 2_000_000_000L)
        public void testFixedRateTask() {
            // intentionally empty
        }

        @Override
        public void configureTasks(@NonNull ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
        }

        public static class CustomTestTask implements Runnable {
            @Override
            public void run() {
                // intentionally empty
            }

            @Override
            public String toString() {
                return CustomTestTask.class.getName();
            }
        }

        public static class CustomTestTrigger implements Trigger {
            @Override
            public Date nextExecutionTime(@NonNull TriggerContext triggerContext) {
                return Date.from(Instant.now().plusSeconds(60 * 60 * 24));
            }
        }
    }
}
