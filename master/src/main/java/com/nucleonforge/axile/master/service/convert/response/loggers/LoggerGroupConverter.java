package com.nucleonforge.axile.master.service.convert.response.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfileResponse;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link LoggerGroup} to {@link GroupProfileResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class LoggerGroupConverter implements Converter<LoggerGroup, GroupProfileResponse> {

    @Override
    public @NonNull GroupProfileResponse convertInternal(@NonNull LoggerGroup source) {
        return new GroupProfileResponse(source.configuredLevel(), source.members());
    }
}
