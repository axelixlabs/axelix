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
package com.axelixlabs.axelix.master.service.convert.response;

import java.util.List;

import jakarta.annotation.Nullable;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;
import com.axelixlabs.axelix.master.api.response.ScheduledTasksResponse;

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
                source.getCron().stream().map(this::convertCronTask).toList();
        List<ScheduledTasksResponse.FixedRate> fixedRateList =
                source.getFixedRate().stream().map(this::convertFixedRateTask).toList();
        List<ScheduledTasksResponse.FixedDelay> fixedDelayList =
                source.getFixedDelay().stream().map(this::convertFixedDelayTask).toList();
        List<ScheduledTasksResponse.Custom> customList =
                source.getCustom().stream().map(this::convertCustomTask).toList();

        return new ScheduledTasksResponse(cronList, fixedDelayList, fixedRateList, customList);
    }

    private ScheduledTasksResponse.Cron convertCronTask(ServiceScheduledTasks.CronTask cron) {
        return new ScheduledTasksResponse.Cron(
                cron.isEnabled(),
                convertRunnable(cron.getRunnable()),
                cron.getExpression(),
                convertNextExecution(cron.getNextExecution()),
                convertLastExecution(cron.getLastExecution()));
    }

    private ScheduledTasksResponse.FixedRate convertFixedRateTask(ServiceScheduledTasks.FixedRateTask fixedRate) {
        return new ScheduledTasksResponse.FixedRate(
                fixedRate.isEnabled(),
                convertRunnable(fixedRate.getRunnable()),
                fixedRate.getInterval(),
                fixedRate.getInitialDelay(),
                convertNextExecution(fixedRate.getNextExecution()),
                convertLastExecution(fixedRate.getLastExecution()));
    }

    private ScheduledTasksResponse.FixedDelay convertFixedDelayTask(ServiceScheduledTasks.FixedDelayTask fixedDelay) {
        return new ScheduledTasksResponse.FixedDelay(
                fixedDelay.isEnabled(),
                convertRunnable(fixedDelay.getRunnable()),
                fixedDelay.getInterval(),
                fixedDelay.getInitialDelay(),
                convertNextExecution(fixedDelay.getNextExecution()),
                convertLastExecution(fixedDelay.getLastExecution()));
    }

    private ScheduledTasksResponse.Custom convertCustomTask(ServiceScheduledTasks.CustomTask custom) {
        return new ScheduledTasksResponse.Custom(
                custom.isEnabled(),
                convertRunnable(custom.getRunnable()),
                custom.getTrigger(),
                convertNextExecution(custom.getNextExecution()),
                convertLastExecution(custom.getLastExecution()));
    }

    private ScheduledTasksResponse.Runnable convertRunnable(ServiceScheduledTasks.Runnable runnable) {
        return new ScheduledTasksResponse.Runnable(runnable.getTarget());
    }

    private @Nullable ScheduledTasksResponse.NextExecution convertNextExecution(
            @Nullable ServiceScheduledTasks.NextExecution nextExecution) {
        return nextExecution != null ? new ScheduledTasksResponse.NextExecution(nextExecution.getTime()) : null;
    }

    private @Nullable ScheduledTasksResponse.LastExecution convertLastExecution(
            @Nullable ServiceScheduledTasks.LastExecution lastExecution) {
        return lastExecution != null
                ? new ScheduledTasksResponse.LastExecution(
                        lastExecution.getStatus(), lastExecution.getTime(), convertException(lastExecution))
                : null;
    }

    private @Nullable ScheduledTasksResponse.LastExecution.Exception convertException(
            ServiceScheduledTasks.LastExecution lastExecution) {
        return lastExecution.getException() != null
                ? new ScheduledTasksResponse.LastExecution.Exception(
                        lastExecution.getException().getType(),
                        lastExecution.getException().getMessage())
                : null;
    }
}
