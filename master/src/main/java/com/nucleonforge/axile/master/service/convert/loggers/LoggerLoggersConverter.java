package com.nucleonforge.axile.master.service.convert.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerLoggers;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link LoggerLoggers} to {@link LoggerProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class LoggerLoggersConverter implements Converter<LoggerLoggers, LoggerProfile> {

    @Override
    public @NonNull LoggerProfile convertInternal(@NonNull LoggerLoggers source) {
        return new LoggerProfile(source.configuredLevel(), source.effectiveLevel());
    }
}
