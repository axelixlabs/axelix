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
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.PropertySource;
import com.axelixlabs.axelix.master.api.response.EnvironmentFeedResponse;
import com.axelixlabs.axelix.master.api.response.EnvironmentFeedResponse.InjectionPoint;
import com.axelixlabs.axelix.master.api.response.EnvironmentFeedResponse.InjectionType;
import com.axelixlabs.axelix.master.api.response.EnvironmentFeedResponse.PropertyEntry;
import com.axelixlabs.axelix.master.api.response.EnvironmentFeedResponse.PropertySourceShortProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link EnvironmentFeed} to {@link EnvironmentFeedResponse}.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Service
public class EnvironmentFeedConverter implements Converter<EnvironmentFeed, EnvironmentFeedResponse> {

    @Override
    public @NonNull EnvironmentFeedResponse convertInternal(@NonNull EnvironmentFeed source) {
        List<String> activeProfiles = source.getActiveProfiles();
        List<String> defaultProfiles = source.getDefaultProfiles();
        List<PropertySourceShortProfile> propertySources = new ArrayList<>();

        for (PropertySource propertySource : source.getPropertySources()) {
            List<PropertyEntry> properties = convertPropertyEntries(propertySource);
            propertySources.add(new PropertySourceShortProfile(
                    propertySource.getSourceName(), propertySource.getSourceDescription(), properties));
        }

        return new EnvironmentFeedResponse(activeProfiles, defaultProfiles, propertySources);
    }

    private List<PropertyEntry> convertPropertyEntries(PropertySource propertySource) {
        List<PropertyEntry> properties = new ArrayList<>();

        if (propertySource.getProperties() != null) {
            for (EnvironmentFeed.Property property : propertySource.getProperties()) {
                properties.add(new PropertyEntry(
                        property.getPropertyName(),
                        property.getValue(),
                        property.isPrimary(),
                        property.getConfigPropsBeanName(),
                        property.getDescription(),
                        mapDeprecation(property.getDeprecation()),
                        mapInjectionPoints(property.getInjectionPoints())));
            }
        }

        return properties;
    }

    private EnvironmentFeedResponse.@Nullable Deprecation mapDeprecation(
            EnvironmentFeed.@Nullable Deprecation deprecation) {
        if (deprecation == null) {
            return null;
        }

        return new EnvironmentFeedResponse.Deprecation(deprecation.getMessage());
    }

    private @Nullable List<EnvironmentFeedResponse.InjectionPoint> mapInjectionPoints(
            @Nullable List<EnvironmentFeed.InjectionPoint> injectionPoints) {
        if (injectionPoints == null || injectionPoints.isEmpty()) {
            return null;
        }

        List<EnvironmentFeedResponse.InjectionPoint> result = new ArrayList<>();
        for (EnvironmentFeed.InjectionPoint ip : injectionPoints) {
            result.add(new InjectionPoint(
                    ip.getBeanName(),
                    mapInjectionType(ip.getInjectionType()),
                    ip.getTargetName(),
                    ip.getPropertyExpression()));
        }
        return result;
    }

    private InjectionType mapInjectionType(EnvironmentFeed.InjectionType injectionType) {
        return InjectionType.valueOf(injectionType.name());
    }
}
