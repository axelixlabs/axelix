package com.nucleonforge.axile.master.service.convert.info.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.master.api.response.info.components.OSProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link OSInfo} to {@link OSProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class OSInfoConverter implements Converter<OSInfo, OSProfile> {

    @Override
    public @NonNull OSProfile convertInternal(@NonNull OSInfo source) {
        return new OSProfile(source.name(), source.version(), source.arch());
    }
}
