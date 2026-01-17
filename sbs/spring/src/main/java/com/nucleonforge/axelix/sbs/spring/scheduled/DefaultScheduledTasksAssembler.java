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

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;

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
