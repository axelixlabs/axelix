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
package com.axelixlabs.axelix.master.service.convert.response.environment;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.env.EnvironmentProperty;
import com.axelixlabs.axelix.master.api.external.response.EnvironmentPropertyResponse;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link EnvironmentProperty} to {@link EnvironmentPropertyResponse}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
@Service
public class EnvironmentPropertyConverter implements Converter<EnvironmentProperty, EnvironmentPropertyResponse> {

    @Override
    public @NonNull EnvironmentPropertyResponse convertInternal(@NonNull EnvironmentProperty environmentProperty) {

        String propertySource = environmentProperty.property().source();
        String propertyValue = environmentProperty.property().value();
        List<EnvironmentPropertyResponse.PropertySource> responseSources = new ArrayList<>();

        for (EnvironmentProperty.SourceEntry entry : environmentProperty.propertySources()) {
            if (entry.property() != null) {
                EnvironmentPropertyResponse.Property property = new EnvironmentPropertyResponse.Property(
                        entry.property().value(), entry.property().origin());
                responseSources.add(new EnvironmentPropertyResponse.PropertySource(entry.sourceName(), property));
            }
        }

        return new EnvironmentPropertyResponse(propertySource, propertyValue, responseSources);
    }
}
