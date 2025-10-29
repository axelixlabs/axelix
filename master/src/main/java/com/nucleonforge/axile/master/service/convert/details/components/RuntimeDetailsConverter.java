package com.nucleonforge.axile.master.service.convert.details.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.common.api.details.components.RuntimeDetails;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.api.response.details.components.RuntimeProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link OsDetails} to {@link OSProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class RuntimeDetailsConverter implements Converter<RuntimeDetails, RuntimeProfile> {

    @Override
    public @NonNull RuntimeProfile convertInternal(@NonNull RuntimeDetails source) {
        return new RuntimeProfile(
                source.javaVersion(), source.kotlinVersion(), source.jdkVendor(), source.garbageCollector());
    }
}
