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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.TriggerTask;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry}
 *
 * @author Nikita Kirillov
 * @since 14.10.2025
 */
class ScheduledTasksRegistryTest extends AbstractScheduledIntegrationTest {
    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @Test
    void shouldRegisterAllScheduledTasks() {
        Collection<ManagedScheduledTask> registeredTasks = taskRegistry.getAll();

        assertThat(registeredTasks).isNotNull().isNotEmpty().hasSize(8);

        List<String> taskIds =
            registeredTasks.stream().map(ManagedScheduledTask::getId).collect(Collectors.toList());

        assertThat(taskIds)
            .hasSize(8)
            .allSatisfy(id -> assertThat(id).isNotBlank())
            .containsExactlyInAnyOrder(CRON_TASK_ID, FIXED_RATE_TASK_ID_FOR_EXECUTE, FIXED_RATE_TASK_ID_FOR_MODIFY, FIXED_DELAY_TASK_ID, FIXED_DELAY_TASK_ID_FOR_MODIFY, CUSTOM_TASK_ID, CRON_TASK_ID_FOR_MODIFY, FIXED_RATE_TASK_ID);

        assertThat(registeredTasks)
            .allSatisfy(task -> assertThat(task.getFuture().isCancelled()).isFalse());

        taskIds.forEach(id -> assertThat(taskRegistry.find(id))
            .isPresent()
            .get()
            .extracting(ManagedScheduledTask::getId)
            .isEqualTo(id));

        assertThat(taskRegistry.find("non-existent-task")).isEmpty();
    }

    @Test
    void shouldHaveCorrectTaskTypes() {
        Collection<ManagedScheduledTask> tasks = taskRegistry.getAll();

        assertThat(tasks)
            .hasSize(8)
            .extracting(ManagedScheduledTask::getScheduledTask)
            .extracting(ScheduledTask::getTask)
            .satisfiesExactlyInAnyOrder(
                task -> assertThat(task).isInstanceOf(CronTask.class),
                task -> assertThat(task).isInstanceOf(CronTask.class),
                task -> assertThat(task).isInstanceOf(FixedDelayTask.class),
                task -> assertThat(task).isInstanceOf(FixedDelayTask.class),
                task -> assertThat(task).isInstanceOf(FixedRateTask.class),
                task -> assertThat(task).isInstanceOf(FixedRateTask.class),
                task -> assertThat(task).isInstanceOf(FixedRateTask.class),
                task -> assertThat(task).isInstanceOf(TriggerTask.class).isNotInstanceOf(CronTask.class));
    }
}
