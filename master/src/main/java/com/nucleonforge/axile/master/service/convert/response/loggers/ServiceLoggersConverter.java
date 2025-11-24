package com.nucleonforge.axile.master.service.convert.response.loggers;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link ServiceLoggers} to {@link LoggersResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceLoggersConverter implements Converter<ServiceLoggers, LoggersResponse> {

    @Override
    public @NonNull LoggersResponse convertInternal(@NonNull ServiceLoggers source) {

        List<LoggersResponse.Group> groups = convertGroup(source);
        List<LoggersResponse.Logger> loggers = convertLogger(source);

        return new LoggersResponse(source.levels(), groups, loggers);
    }

    private List<LoggersResponse.Logger> convertLogger(ServiceLoggers source) {
        return source.loggers().entrySet().stream()
                .map(logger -> new LoggersResponse.Logger(
                        logger.getKey(),
                        logger.getValue().configuredLevel(),
                        logger.getValue().effectiveLevel()))
                .toList();
    }

    private List<LoggersResponse.Group> convertGroup(ServiceLoggers source) {
        return source.groups().entrySet().stream()
                .map(group -> new LoggersResponse.Group(
                        group.getKey(),
                        group.getValue().configuredLevel(),
                        group.getValue().members()))
                .toList();
    }
}
