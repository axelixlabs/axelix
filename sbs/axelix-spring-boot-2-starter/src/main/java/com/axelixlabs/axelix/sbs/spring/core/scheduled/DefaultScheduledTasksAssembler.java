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

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ScheduledCronTask;
import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ScheduledCustomTask;
import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ScheduledFixedDelayTask;
import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ScheduledFixedRateTask;
import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ScheduledTaskRunnable;
import com.axelixlabs.axelix.sbs.spring.core.contract.scheduledtask.ServiceScheduledTasks;

/**
 * Default implementation of {@link ScheduledTasksAssembler}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public class DefaultScheduledTasksAssembler implements ScheduledTasksAssembler, AutoCloseable {

    private final ScheduledTasksRegistry registry;

    public DefaultScheduledTasksAssembler(ScheduledTasksRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ServiceScheduledTasks assemble() {
        List<ScheduledCronTask> cron = new ArrayList<>();
        List<ScheduledFixedDelayTask> fixedDelay = new ArrayList<>();
        List<ScheduledFixedRateTask> fixedRate = new ArrayList<>();
        List<ScheduledCustomTask> custom = new ArrayList<>();

        registry.getAll().forEach(task -> assembleScheduledTasks(task, cron, fixedDelay, fixedRate, custom));

        return new ServiceScheduledTasks()
                .cron(cron)
                .fixedDelay(fixedDelay)
                .fixedRate(fixedRate)
                .custom(custom);
    }

    private void assembleScheduledTasks(
            ManagedScheduledTask managedScheduledTask,
            List<ScheduledCronTask> cron,
            List<ScheduledFixedDelayTask> fixedDelay,
            List<ScheduledFixedRateTask> fixedRate,
            List<ScheduledCustomTask> custom) {

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

    private ScheduledCronTask assembleCronTask(CronTask task, ManagedScheduledTask managedScheduledTask) {
        String target = managedScheduledTask.getRunnable().toString();

        return new ScheduledCronTask()
                .runnable(new ScheduledTaskRunnable().target(target))
                .expression(task.getExpression())
                .enabled(managedScheduledTask.isEnabled());
    }

    private ScheduledFixedRateTask assembleFixedRateTask(
            FixedRateTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ScheduledFixedRateTask()
                .runnable(new ScheduledTaskRunnable().target(target))
                .interval(task.getInterval())
                .initialDelay(task.getInitialDelay())
                .enabled(managedScheduledTask.isEnabled());
    }

    private ScheduledFixedDelayTask assembleFixedDelayMap(
            FixedDelayTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ScheduledFixedDelayTask()
                .runnable(new ScheduledTaskRunnable().target(target))
                .interval(task.getInterval())
                .initialDelay(task.getInitialDelay())
                .enabled(managedScheduledTask.isEnabled());
    }

    private ScheduledCustomTask assembleCustomMap(TriggerTask task, ManagedScheduledTask managedScheduledTask) {
        String target = task.getRunnable().toString();

        return new ScheduledCustomTask()
                .runnable(new ScheduledTaskRunnable().target(target))
                .trigger(task.getTrigger().toString())
                .enabled(managedScheduledTask.isEnabled());
    }

    @Override
    public void close() {
        if (this.registry != null) {
            this.registry.close();
        }
    }
}
