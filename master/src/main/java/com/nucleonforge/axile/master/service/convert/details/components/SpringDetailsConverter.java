package com.nucleonforge.axile.master.service.convert.details.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.common.api.details.components.SpringDetails;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.api.response.details.components.SpringProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link OsDetails} to {@link OSProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class SpringDetailsConverter implements Converter<SpringDetails, SpringProfile> {

    @Override
    public @NonNull SpringProfile convertInternal(@NonNull SpringDetails source) {
        return new SpringProfile(
                source.springBootVersion(), source.springFrameworkVersion(), source.springCloudVersion());
    }
}
