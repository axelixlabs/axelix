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
package com.nucleonforge.axelix.master.service.convert.response;

import java.util.List;

import jakarta.annotation.Nullable;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;
import com.nucleonforge.axelix.master.api.response.ScheduledTasksResponse;

/**
 * The {@link Converter} from {@link ServiceScheduledTasks} to {@link ScheduledTasksResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceScheduledTasksConverter implements Converter<ServiceScheduledTasks, ScheduledTasksResponse> {

    @Override
    public @NonNull ScheduledTasksResponse convertInternal(@NonNull ServiceScheduledTasks source) {
        List<ScheduledTasksResponse.Cron> cronList =
                source.cron().stream().map(this::convertCronTask).toList();
        List<ScheduledTasksResponse.FixedRate> fixedRateList =
                source.fixedRate().stream().map(this::convertFixedRateTask).toList();
        List<ScheduledTasksResponse.FixedDelay> fixedDelayList =
                source.fixedDelay().stream().map(this::convertFixedDelayTask).toList();
        List<ScheduledTasksResponse.Custom> customList =
                source.custom().stream().map(this::convertCustomTask).toList();

        return new ScheduledTasksResponse(cronList, fixedDelayList, fixedRateList, customList);
    }

    private ScheduledTasksResponse.Cron convertCronTask(ServiceScheduledTasks.CronTask cron) {
        return new ScheduledTasksResponse.Cron(
                cron.enabled(),
                convertRunnable(cron.runnable()),
                cron.expression(),
                convertNextExecution(cron.nextExecution()),
                convertLastExecution(cron.lastExecution()));
    }

    private ScheduledTasksResponse.FixedRate convertFixedRateTask(ServiceScheduledTasks.FixedRateTask fixedRate) {
        return new ScheduledTasksResponse.FixedRate(
                fixedRate.enabled(),
                convertRunnable(fixedRate.runnable()),
                fixedRate.interval(),
                fixedRate.initialDelay(),
                convertNextExecution(fixedRate.nextExecution()),
                convertLastExecution(fixedRate.lastExecution()));
    }

    private ScheduledTasksResponse.FixedDelay convertFixedDelayTask(ServiceScheduledTasks.FixedDelayTask fixedDelay) {
        return new ScheduledTasksResponse.FixedDelay(
                fixedDelay.enabled(),
                convertRunnable(fixedDelay.runnable()),
                fixedDelay.interval(),
                fixedDelay.initialDelay(),
                convertNextExecution(fixedDelay.nextExecution()),
                convertLastExecution(fixedDelay.lastExecution()));
    }

    private ScheduledTasksResponse.Custom convertCustomTask(ServiceScheduledTasks.CustomTask custom) {
        return new ScheduledTasksResponse.Custom(
                custom.enabled(),
                convertRunnable(custom.runnable()),
                custom.trigger(),
                convertNextExecution(custom.nextExecution()),
                convertLastExecution(custom.lastExecution()));
    }

    private ScheduledTasksResponse.Runnable convertRunnable(ServiceScheduledTasks.Runnable runnable) {
        return new ScheduledTasksResponse.Runnable(runnable.target());
    }

    private @Nullable ScheduledTasksResponse.NextExecution convertNextExecution(
            @Nullable ServiceScheduledTasks.NextExecution nextExecution) {
        return nextExecution != null ? new ScheduledTasksResponse.NextExecution(nextExecution.time()) : null;
    }

    private @Nullable ScheduledTasksResponse.LastExecution convertLastExecution(
            @Nullable ServiceScheduledTasks.LastExecution lastExecution) {
        return lastExecution != null
                ? new ScheduledTasksResponse.LastExecution(
                        lastExecution.status(), lastExecution.time(), convertException(lastExecution))
                : null;
    }

    private @Nullable ScheduledTasksResponse.LastExecution.Exception convertException(
            ServiceScheduledTasks.LastExecution lastExecution) {
        return lastExecution.exception() != null
                ? new ScheduledTasksResponse.LastExecution.Exception(
                        lastExecution.exception().type(),
                        lastExecution.exception().message())
                : null;
    }
}
