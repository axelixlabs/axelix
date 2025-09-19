package com.nucleonforge.axile.master.service.convert.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.GroupLoggers;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link GroupLoggers} to {@link GroupProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class GroupLoggersConverter implements Converter<GroupLoggers, GroupProfile> {

    @Override
    public @NonNull GroupProfile convertInternal(@NonNull GroupLoggers source) {
        return new GroupProfile(source.configuredLevel(), source.members());
    }
}
