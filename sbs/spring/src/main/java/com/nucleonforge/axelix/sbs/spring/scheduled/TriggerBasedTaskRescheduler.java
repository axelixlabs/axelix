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

import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
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
    public void reschedule(ManagedScheduledTask task) {
        Trigger trigger = task.getTrigger();

        Assert.notNull(trigger, "Trigger cannot be null at this point");

        ScheduledFuture<?> rescheduledFuture = taskScheduler.schedule(task.getRunnable(), trigger);

        // rescheduledFuture may be null in case the supplied Trigger won't fire anymore,
        // therefore, there is nothing to schedule.
        if (rescheduledFuture != null) {
            task.replaceScheduledFuture(rescheduledFuture);
        }
    }

    @Override
    public boolean supports(ManagedScheduledTask task) {
        return task.getTrigger() != null;
    }

    @Override
    public void mutate(ManagedScheduledTask task, String newExpression) {
        Trigger trigger = task.getTrigger();

        Assert.notNull(trigger, "Trigger cannot be null at this point");

        CronTrigger newTrigger = new CronTrigger(newExpression);

        ScheduledFuture<?> rescheduledFuture = taskScheduler.schedule(task.getRunnable(), newTrigger);

        // rescheduledFuture may be null in case the supplied Trigger won't fire anymore,
        // therefore, there is nothing to schedule.
        if (rescheduledFuture != null) {
            task.replaceScheduledFuture(rescheduledFuture);
            task.setCronTrigger(newTrigger);
        }
    }
}
