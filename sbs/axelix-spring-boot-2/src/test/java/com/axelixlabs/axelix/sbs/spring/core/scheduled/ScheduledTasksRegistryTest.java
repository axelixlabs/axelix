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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.shared.SharedEndpointTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
class ScheduledTasksRegistryTest extends AbstractEndpointTest {

    private static final String CRON_TASK_ID = SharedEndpointTestConfiguration.class.getName() + ".testCronTask";
    private static final String FIXED_DELAY_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String CUSTOM_TASK_ID = SharedEndpointTestConfiguration.CustomTestTask.class.getName();

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

        // The shared Spring context registers a superset of scheduled tasks via
        // {@link SharedEndpointTestConfiguration}; this test verifies that the four task variants this class cares
        // about (cron / fixedDelay / fixedRate / custom trigger) are all present and addressable.
        assertThat(taskIds)
                .allSatisfy(id -> assertThat(id).isNotBlank())
                .containsAll(EXPECTED_TASK_IDS);

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
}
