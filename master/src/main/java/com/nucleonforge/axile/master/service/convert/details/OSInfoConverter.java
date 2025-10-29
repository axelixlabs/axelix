package com.nucleonforge.axile.master.service.convert.details;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.OSInfo;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
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
