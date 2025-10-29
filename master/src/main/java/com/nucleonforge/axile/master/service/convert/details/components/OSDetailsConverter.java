package com.nucleonforge.axile.master.service.convert.details.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link OsDetails} to {@link OSProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class OSDetailsConverter implements Converter<OsDetails, OSProfile> {

    @Override
    public @NonNull OSProfile convertInternal(@NonNull OsDetails source) {
        return new OSProfile(source.name(), source.version(), source.arch());
    }
}
