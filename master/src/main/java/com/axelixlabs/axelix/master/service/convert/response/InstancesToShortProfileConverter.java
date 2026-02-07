/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.convert.response;

import java.time.Duration;
import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceShortProfile;
import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceStatus;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.convert.utils.DateTimeFormattingUtils;

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
                    case RELOAD -> InstanceStatus.RELOAD;
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
