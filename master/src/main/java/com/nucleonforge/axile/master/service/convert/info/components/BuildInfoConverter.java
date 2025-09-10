package com.nucleonforge.axile.master.service.convert.info.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link BuildInfo} to {@link BuildProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class BuildInfoConverter implements Converter<BuildInfo, BuildProfile> {

    @Override
    public @NonNull BuildProfile convertInternal(@NonNull BuildInfo source) {
        return new BuildProfile(source.artifact(), source.name(), source.version(), source.group(), source.time());
    }
}
