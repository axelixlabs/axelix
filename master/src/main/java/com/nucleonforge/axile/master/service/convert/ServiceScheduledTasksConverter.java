package com.nucleonforge.axile.master.service.convert;

import java.util.List;

import jakarta.annotation.Nullable;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.master.api.response.ScheduledTasksResponse;

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
                convertRunnable(cron.delegate().runnable()),
                cron.delegate().expression(),
                convertNextExecution(cron.delegate().nextExecution()),
                convertLastExecution(cron.delegate().lastExecution()));
    }

    private ScheduledTasksResponse.FixedRate convertFixedRateTask(ServiceScheduledTasks.FixedRateTask fixedRate) {
        return new ScheduledTasksResponse.FixedRate(
                fixedRate.enabled(),
                convertRunnable(fixedRate.delegate().runnable()),
                fixedRate.delegate().interval(),
                fixedRate.delegate().initialDelay(),
                convertNextExecution(fixedRate.delegate().nextExecution()),
                convertLastExecution(fixedRate.delegate().lastExecution()));
    }

    private ScheduledTasksResponse.FixedDelay convertFixedDelayTask(ServiceScheduledTasks.FixedDelayTask fixedDelay) {
        return new ScheduledTasksResponse.FixedDelay(
                fixedDelay.enabled(),
                convertRunnable(fixedDelay.delegate().runnable()),
                fixedDelay.delegate().interval(),
                fixedDelay.delegate().initialDelay(),
                convertNextExecution(fixedDelay.delegate().nextExecution()),
                convertLastExecution(fixedDelay.delegate().lastExecution()));
    }

    private ScheduledTasksResponse.Custom convertCustomTask(ServiceScheduledTasks.CustomTask custom) {
        return new ScheduledTasksResponse.Custom(
                custom.enabled(),
                convertRunnable(custom.delegate().runnable()),
                custom.delegate().trigger(),
                convertLastExecution(custom.delegate().lastExecution()));
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
