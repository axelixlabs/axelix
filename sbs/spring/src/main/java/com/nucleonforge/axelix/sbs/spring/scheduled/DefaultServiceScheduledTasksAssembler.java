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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;

/**
 * Default implementation of {@link ServiceScheduledTasksAssembler}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultServiceScheduledTasksAssembler implements ServiceScheduledTasksAssembler {

    private final Collection<ScheduledTaskHolder> scheduledTaskHolders;
    private final ScheduledTaskService taskService;

    public DefaultServiceScheduledTasksAssembler(
            Collection<ScheduledTaskHolder> scheduledTaskHolders, ScheduledTaskService taskService) {
        this.scheduledTaskHolders = scheduledTaskHolders;
        this.taskService = taskService;
    }

    @Override
    public ServiceScheduledTasks assemble() {
        List<ServiceScheduledTasks.CronTask> cron = new ArrayList<>();
        List<ServiceScheduledTasks.FixedDelayTask> fixedDelay = new ArrayList<>();
        List<ServiceScheduledTasks.FixedRateTask> fixedRate = new ArrayList<>();
        List<ServiceScheduledTasks.CustomTask> custom = new ArrayList<>();

        scheduledTaskHolders.stream()
                .flatMap(h -> h.getScheduledTasks().stream())
                .map(ScheduledTask::getTask)
                .forEach(task -> assembleScheduledTasks(task, cron, fixedDelay, fixedRate, custom));

        return new ServiceScheduledTasks(cron, fixedDelay, fixedRate, custom);
    }

    private void assembleScheduledTasks(
            Task task,
            List<ServiceScheduledTasks.CronTask> cron,
            List<ServiceScheduledTasks.FixedDelayTask> fixedDelay,
            List<ServiceScheduledTasks.FixedRateTask> fixedRate,
            List<ServiceScheduledTasks.CustomTask> custom) {
        if (task instanceof CronTask cronTask) {
            cron.add(assembleCronTask(cronTask));
        } else if (task instanceof FixedRateTask fixedRateTask) {
            fixedRate.add(assembleFixedRateTask(fixedRateTask));
        } else if (task instanceof FixedDelayTask fixedDelayTask) {
            fixedDelay.add(assembleFixedDelayMap(fixedDelayTask));
        } else if (task instanceof TriggerTask customTriggerTask) {
            custom.add(assembleCustomMap(customTriggerTask));
        }
    }

    private ServiceScheduledTasks.CronTask assembleCronTask(CronTask task) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.CronTask(
                new ServiceScheduledTasks.Runnable(target),
                extractCronExpression(target),
                null,
                null,
                extractTaskEnabledStatus(target));
    }

    private ServiceScheduledTasks.FixedRateTask assembleFixedRateTask(FixedRateTask task) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedRateTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getIntervalDuration().toMillis(),
                task.getInitialDelayDuration().toMillis(),
                null,
                null,
                extractTaskEnabledStatus(target));
    }

    private ServiceScheduledTasks.FixedDelayTask assembleFixedDelayMap(FixedDelayTask task) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.FixedDelayTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getIntervalDuration().toMillis(),
                task.getInitialDelayDuration().toMillis(),
                null,
                null,
                extractTaskEnabledStatus(target));
    }

    private ServiceScheduledTasks.CustomTask assembleCustomMap(TriggerTask task) {
        String target = task.getRunnable().toString();

        return new ServiceScheduledTasks.CustomTask(
                new ServiceScheduledTasks.Runnable(target),
                task.getTrigger().toString(),
                null,
                null,
                extractTaskEnabledStatus(target));
    }

    private boolean extractTaskEnabledStatus(String target) {
        // TODO:
        //  1. how is that possible that future will be null?
        //  2. is that correct that we're returning tru in case the task is not found? I guess no.
        return taskService
                .find(target)
                .map(task -> {
                    ScheduledFuture<?> future = task.getFuture();
                    return future == null || !future.isCancelled();
                })
                .orElse(true);
    }

    private @Nullable String extractCronExpression(String target) {
        return taskService
                .find(target)
                .map(task -> {
                    CronTrigger trigger = task.getCronTrigger();
                    return trigger != null ? trigger.getExpression() : null;
                })
                .orElse(null);
    }
}
