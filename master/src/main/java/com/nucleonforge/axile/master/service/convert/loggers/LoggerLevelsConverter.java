package com.nucleonforge.axile.master.service.convert.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link LoggerLevels} to {@link LoggerProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class LoggerLevelsConverter implements Converter<LoggerLevels, LoggerProfile> {

    @Override
    public @NonNull LoggerProfile convertInternal(@NonNull LoggerLevels source) {
        return new LoggerProfile(source.configuredLevel(), source.effectiveLevel());
    }
}
