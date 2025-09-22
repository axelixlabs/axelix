package com.nucleonforge.axile.master.service.convert.loggers;

import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfile;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfile;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link ServiceLoggers} to {@link LoggersResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceLoggersConverter implements Converter<ServiceLoggers, LoggersResponse> {
    private final LoggerGroupConverter loggerGroupConverter;
    private final LoggerLevelsConverter loggerLevelsConverter;

    public ServiceLoggersConverter(
            LoggerGroupConverter loggerGroupConverter, LoggerLevelsConverter loggerLevelsConverter) {
        this.loggerGroupConverter = loggerGroupConverter;
        this.loggerLevelsConverter = loggerLevelsConverter;
    }

    @Override
    public @NonNull LoggersResponse convertInternal(@NonNull ServiceLoggers source) {

        Map<String, GroupProfile> groups = source.groups().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> loggerGroupConverter.convert(e.getValue())));

        Map<String, LoggerProfile> loggers = source.loggers().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> loggerLevelsConverter.convert(e.getValue())));

        return new LoggersResponse(source.levels(), groups, loggers);
    }
}
