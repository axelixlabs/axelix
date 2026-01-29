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

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;

/**
 * {@link TaskRescheduler} that is capable to re-schedule the {@link FixedDelayTask} and
 * {@link FixedRateTask} tasks.
 *
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 */
public final class IntervalBasedTaskRescheduler implements TaskRescheduler {

    private final TaskScheduler taskScheduler;

    public IntervalBasedTaskRescheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void reschedule(ManagedScheduledTask task, Task newTask) {
        if (newTask instanceof FixedDelayTask fixedDelayTask) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                    newTask.getRunnable(),
                    Instant.now().plus(fixedDelayTask.getInitialDelayDuration()),
                    fixedDelayTask.getIntervalDuration());

            task.replaceScheduledState(scheduledFuture, newTask);
        }

        if (newTask instanceof FixedRateTask fixedRateTask) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(
                    newTask.getRunnable(),
                    Instant.now().plus(fixedRateTask.getInitialDelayDuration()),
                    fixedRateTask.getIntervalDuration());

            task.replaceScheduledState(scheduledFuture, newTask);
        }
    }

    @Override
    public boolean supports(ManagedScheduledTask task) {
        if (task.getTrigger() != null) {
            return false;
        }

        Task actualTask = task.getScheduledTask().getTask();

        return actualTask instanceof FixedRateTask || actualTask instanceof FixedDelayTask;
    }
}
