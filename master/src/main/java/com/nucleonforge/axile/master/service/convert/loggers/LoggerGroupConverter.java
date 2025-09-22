package com.nucleonforge.axile.master.service.convert.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link LoggerGroup} to {@link GroupProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class LoggerGroupConverter implements Converter<LoggerGroup, GroupProfile> {

    @Override
    public @NonNull GroupProfile convertInternal(@NonNull LoggerGroup source) {
        return new GroupProfile(source.configuredLevel(), source.members());
    }
}
