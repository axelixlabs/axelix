package com.nucleonforge.axile.master.service.convert;

import java.time.Duration;
import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.api.response.InstancesGridResponse.InstanceShortProfile;
import com.nucleonforge.axile.master.api.response.InstancesGridResponse.InstanceStatus;
import com.nucleonforge.axile.master.instance.Instance;
import com.nucleonforge.axile.master.service.convert.utils.DateTimeFormattingUtils;

/**
 * Converter that is capable to translate the given {@link Instance} to the {@link InstanceShortProfile}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class InstancesToShortProfileConverter implements Converter<Instance, InstanceShortProfile> {

    @Override
    public @NonNull InstanceShortProfile convertInternal(@NonNull Instance instance) {
        return new InstanceShortProfile(
                instance.id().instanceId(),
                instance.name(),
                instance.serviceVersion(),
                instance.commitShaShort(),
                switch (instance.status()) {
                    case UP -> InstanceStatus.UP;
                    case DOWN -> InstanceStatus.DOWN;
                    case UNKNOWN -> InstanceStatus.UNKNOWN;
                },
                buildDeployedForField(instance),
                instance.javaVersion(),
                instance.springBootVersion());
    }

    @Nullable
    private static String buildDeployedForField(Instance instance) {
        Instant deployedAt = instance.deployedAt();

        if (deployedAt != null) {
            Duration duration = Duration.between(deployedAt, Instant.now());

            // TODO:
            //  The very idea of converting anything to human-readable
            //  representation on the backend is wrong. What I'm doing
            //  here is WRONG from the overall design point of view. Later
            //  that needs to be fixed. The problem is that the actual display,
            //  language (i18n), styling etc - all of this is applied on the
            //  front-end side.
            return DateTimeFormattingUtils.toHumanReadableDuration(duration);
        }

        return null;
    }
}
