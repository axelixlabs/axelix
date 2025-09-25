package com.nucleonforge.axile.master.service.convert.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfileResponse;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link LoggerLevels} to {@link LoggerProfileResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class LoggerLevelsConverter implements Converter<LoggerLevels, LoggerProfileResponse> {

    @Override
    public @NonNull LoggerProfileResponse convertInternal(@NonNull LoggerLevels source) {
        return new LoggerProfileResponse(source.configuredLevel(), source.effectiveLevel());
    }
}
