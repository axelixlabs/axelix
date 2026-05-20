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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;
import com.axelixlabs.axelix.common.api.ServiceScheduledTasks.LastExecution;
import com.axelixlabs.axelix.common.api.ServiceScheduledTasks.NextExecution;

/**
 * Default implementation of {@link ScheduledTasksAssembler}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Aleksei Ermakov
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

        if (task instanceof CronTask) {
            CronTask cronTask = (CronTask) task;
            cron.add(assembleCronTask(cronTask, managedScheduledTask));
        } else if (task instanceof FixedRateTask) {
            FixedRateTask fixedRateTask = (FixedRateTask) task;
            fixedRate.add(assembleFixedRateTask(fixedRateTask, managedScheduledTask));
        } else if (task instanceof FixedDelayTask) {
            FixedDelayTask fixedDelayTask = (FixedDelayTask) task;
            fixedDelay.add(assembleFixedDelayMap(fixedDelayTask, managedScheduledTask));
        } else if (task instanceof TriggerTask) {
            TriggerTask customTriggerTask = (TriggerTask) task;
            custom.add(assembleCustomMap(customTriggerTask, managedScheduledTask));
        }
    }

    private ServiceScheduledTasks.CronTask assembleCronTask(CronTask task, ManagedScheduledTask managedScheduledTask) {
        String target = managedScheduledTask.getRunnable().toString();

        return new ServiceScheduledTasks.CronTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getExpression(),
                nextExecutionOf(managedScheduledTask),
                lastExecutionOf(managedScheduledTask),
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.FixedRateTask assembleFixedRateTask(
            FixedRateTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedRateTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getInterval(),
                task.getInitialDelay(),
                nextExecutionOf(managedScheduledTask),
                lastExecutionOf(managedScheduledTask),
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.FixedDelayTask assembleFixedDelayMap(
            FixedDelayTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedDelayTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getInterval(),
                task.getInitialDelay(),
                nextExecutionOf(managedScheduledTask),
                lastExecutionOf(managedScheduledTask),
                managedScheduledTask.isEnabled());
    }

    private ServiceScheduledTasks.CustomTask assembleCustomMap(
            TriggerTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.CustomTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getTrigger().toString(),
                nextExecutionOf(managedScheduledTask),
                lastExecutionOf(managedScheduledTask),
                managedScheduledTask.isEnabled());
    }

    @Nullable
    private NextExecution nextExecutionOf(ManagedScheduledTask task) {
        if (!task.isEnabled()) {
            return null;
        }
        ScheduledFuture<?> future = task.getFuture();
        long delayMs = future.getDelay(TimeUnit.MILLISECONDS);
        if (delayMs < 0) {
            return null;
        }
        return new NextExecution(Instant.now().plusMillis(delayMs).toString());
    }

    @Nullable
    private LastExecution lastExecutionOf(ManagedScheduledTask task) {
        Runnable r = task.getRunnable();
        if (!(r instanceof TrackingRunnable)) {
            return null;
        }
        TrackingRunnable tr = (TrackingRunnable) r;
        String status = tr.getLastStatus();
        if (status == null) {
            return null;
        }
        Instant lastTime = tr.getLastTime();
        if (lastTime == null) {
            return null;
        }
        LastExecution.Exception ex = null;
        Throwable lastException = tr.getLastException();
        if ("ERROR".equals(status) && lastException != null) {
            String exMessage = lastException.getMessage();
            ex = new LastExecution.Exception(lastException.getClass().getName(), exMessage != null ? exMessage : "");
        }
        return new LastExecution(status, lastTime.toString(), ex);
    }
}
