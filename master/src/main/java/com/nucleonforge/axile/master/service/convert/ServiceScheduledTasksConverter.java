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
                source.cron().stream().map(this::convertCron).toList();
        List<ScheduledTasksResponse.FixedRate> fixedRateList =
                source.fixedRate().stream().map(this::convertFixedRate).toList();
        List<ScheduledTasksResponse.FixedDelay> fixedDelayList =
                source.fixedDelay().stream().map(this::convertFixedDelay).toList();
        List<ScheduledTasksResponse.Custom> customList =
                source.custom().stream().map(this::convertCustom).toList();

        return new ScheduledTasksResponse(cronList, fixedDelayList, fixedRateList, customList);
    }

    private ScheduledTasksResponse.Cron convertCron(ServiceScheduledTasks.Cron cron) {
        return new ScheduledTasksResponse.Cron(
                convertRunnable(cron.runnable()),
                cron.expression(),
                convertNextExecution(cron.nextExecution()),
                convertLastExecution(cron.lastExecution()));
    }

    private ScheduledTasksResponse.FixedRate convertFixedRate(ServiceScheduledTasks.FixedRate fixedRate) {
        return new ScheduledTasksResponse.FixedRate(
                convertRunnable(fixedRate.runnable()),
                fixedRate.interval(),
                fixedRate.initialDelay(),
                convertNextExecution(fixedRate.nextExecution()),
                convertLastExecution(fixedRate.lastExecution()));
    }

    private ScheduledTasksResponse.FixedDelay convertFixedDelay(ServiceScheduledTasks.FixedDelay fixedDelay) {
        return new ScheduledTasksResponse.FixedDelay(
                convertRunnable(fixedDelay.runnable()),
                fixedDelay.interval(),
                fixedDelay.initialDelay(),
                convertNextExecution(fixedDelay.nextExecution()),
                convertLastExecution(fixedDelay.lastExecution()));
    }

    private ScheduledTasksResponse.Custom convertCustom(ServiceScheduledTasks.Custom custom) {
        return new ScheduledTasksResponse.Custom(
                convertRunnable(custom.runnable()), custom.trigger(), convertLastExecution(custom.lastExecution()));
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
