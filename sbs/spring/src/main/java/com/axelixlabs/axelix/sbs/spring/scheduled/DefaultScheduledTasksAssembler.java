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
package com.axelixlabs.axelix.sbs.spring.scheduled;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;

/**
 * Default implementation of {@link ScheduledTasksAssembler}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultScheduledTasksAssembler implements ScheduledTasksAssembler {

    private final ScheduledTasksRegistry registry;

    public DefaultScheduledTasksAssembler(ScheduledTasksRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ServiceScheduledTasks assemble() {
        List<ServiceScheduledTasks.CronTask> cron = new ArrayList<>();
        List<ServiceScheduledTasks.FixedDelayTask> fixedDelay = new ArrayList<>();
        List<ServiceScheduledTasks.FixedRateTask> fixedRate = new ArrayList<>();
        List<ServiceScheduledTasks.CustomTask> custom = new ArrayList<>();

        registry.getAll().forEach(task -> assembleScheduledTasks(task, cron, fixedDelay, fixedRate, custom));

        return new ServiceScheduledTasks(cron, fixedDelay, fixedRate, custom);
    }

    private void assembleScheduledTasks(
            ManagedScheduledTask managedScheduledTask,
            List<ServiceScheduledTasks.CronTask> cron,
            List<ServiceScheduledTasks.FixedDelayTask> fixedDelay,
            List<ServiceScheduledTasks.FixedRateTask> fixedRate,
            List<ServiceScheduledTasks.CustomTask> custom) {

        Task task = managedScheduledTask.getScheduledTask().getTask();

        if (task instanceof CronTask cronTask) {
            cron.add(assembleCronTask(cronTask, managedScheduledTask));
        } else if (task instanceof FixedRateTask fixedRateTask) {
            fixedRate.add(assembleFixedRateTask(fixedRateTask, managedScheduledTask));
        } else if (task instanceof FixedDelayTask fixedDelayTask) {
            fixedDelay.add(assembleFixedDelayMap(fixedDelayTask, managedScheduledTask));
        } else if (task instanceof TriggerTask customTriggerTask) {
            custom.add(assembleCustomMap(customTriggerTask, managedScheduledTask));
        }
    }

    private ServiceScheduledTasks.CronTask assembleCronTask(CronTask task, ManagedScheduledTask managedScheduledTask) {
        String target = managedScheduledTask.getRunnable().toString();

        return new ServiceScheduledTasks.CronTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getExpression(),
                null,
                null,
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.FixedRateTask assembleFixedRateTask(
            FixedRateTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedRateTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getIntervalDuration().toMillis(),
                task.getInitialDelayDuration().toMillis(),
                null,
                null,
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.FixedDelayTask assembleFixedDelayMap(
            FixedDelayTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedDelayTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getIntervalDuration().toMillis(),
                task.getInitialDelayDuration().toMillis(),
                null,
                null,
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.CustomTask assembleCustomMap(
            TriggerTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.CustomTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getTrigger().toString(),
                null,
                null,
                managedScheduledTask.isEnabled());
    }
}
