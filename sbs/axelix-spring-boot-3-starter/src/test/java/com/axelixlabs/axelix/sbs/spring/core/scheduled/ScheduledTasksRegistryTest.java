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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.TriggerTask;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
class ScheduledTasksRegistryTest extends AbstractScheduledSharedContextTest {

    private static final String CRON_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCronTaskForModify";
    private static final String FIXED_DELAY_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTaskForModify";
    private static final String FIXED_RATE_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTaskForExecute";
    private static final String CUSTOM_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCustomTask";

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @Test
    void shouldRegisterAllScheduledTasks() {
        Collection<ManagedScheduledTask> registeredTasks = taskRegistry.getAll();

        List<String> taskIds =
                registeredTasks.stream().map(ManagedScheduledTask::getId).toList();

        assertThat(taskIds)
                .allSatisfy(id -> assertThat(id).isNotBlank())
                .contains(
                        CRON_TASK_ID,
                        CRON_TASK_ID_FOR_MODIFY,
                        FIXED_DELAY_TASK_ID,
                        FIXED_DELAY_TASK_ID_FOR_MODIFY,
                        FIXED_RATE_TASK_ID,
                        FIXED_RATE_TASK_ID_FOR_MODIFY,
                        FIXED_RATE_TASK_ID_FOR_EXECUTE,
                        CUSTOM_TASK_ID);

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
                .extracting(ManagedScheduledTask::getScheduledTask)
                .extracting(ScheduledTask::getTask)
                .hasAtLeastOneElementOfType(CronTask.class)
                .hasAtLeastOneElementOfType(FixedDelayTask.class)
                .hasAtLeastOneElementOfType(FixedRateTask.class)
                .anySatisfy(task ->
                        assertThat(task).isInstanceOf(TriggerTask.class).isNotInstanceOf(CronTask.class));
    }
}
