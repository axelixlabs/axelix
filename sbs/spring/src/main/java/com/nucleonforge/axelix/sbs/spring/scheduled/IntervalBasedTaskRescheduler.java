/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.scheduled;

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
    public void reschedule(ManagedScheduledTask task) {
        Task currentTask = task.getScheduledTask().getTask();

        if (currentTask instanceof FixedDelayTask fixedDelayTask) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                    task.getRunnable(),
                    Instant.now().plus(fixedDelayTask.getInitialDelayDuration()),
                    fixedDelayTask.getIntervalDuration());

            task.replaceScheduledFuture(scheduledFuture);
        }

        if (currentTask instanceof FixedRateTask fixedRateTask) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(
                    task.getRunnable(),
                    Instant.now().plus(fixedRateTask.getInitialDelayDuration()),
                    fixedRateTask.getIntervalDuration());

            task.replaceScheduledFuture(scheduledFuture);
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

    @Override
    public void mutate(ManagedScheduledTask task, String newExpression) {}
}
