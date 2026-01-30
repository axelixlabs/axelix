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

import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.Assert;

/**
 * {@link TaskRescheduler} that re-schedules the {@link Trigger Trigger-based} tasks.
 * The most prominent example is {@link CronTrigger}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 */
public final class TriggerBasedTaskRescheduler implements TaskRescheduler {

    private final TaskScheduler taskScheduler;

    public TriggerBasedTaskRescheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void reschedule(ManagedScheduledTask task, Task newTask) {
        Assert.state(
                newTask instanceof TriggerTask,
                "Expected a new task schedule to be an instance of the TriggerTask, but was %s"
                        .formatted(newTask.getClass()));

        // returned rescheduledFuture may be null in case the supplied Trigger won't fire anymore
        ScheduledFuture<?> rescheduledFuture =
                taskScheduler.schedule(newTask.getRunnable(), ((TriggerTask) newTask).getTrigger());

        task.replaceScheduledState(rescheduledFuture, newTask);
    }

    @Override
    public boolean supports(ManagedScheduledTask task) {
        return task.getTrigger() != null;
    }
}
