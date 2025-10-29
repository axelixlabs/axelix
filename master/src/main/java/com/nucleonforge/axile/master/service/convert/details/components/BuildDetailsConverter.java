package com.nucleonforge.axile.master.service.convert.details.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.BuildDetails;
import com.nucleonforge.axile.master.api.response.details.components.BuildProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link BuildDetails} to {@link BuildProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class BuildDetailsConverter implements Converter<BuildDetails, BuildProfile> {

    @Override
    public @NonNull BuildProfile convertInternal(@NonNull BuildDetails source) {
        return new BuildProfile(source.artifact(), source.version(), source.group(), source.time());
    }
}
