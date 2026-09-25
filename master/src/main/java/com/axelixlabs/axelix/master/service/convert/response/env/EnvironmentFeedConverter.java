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
package com.axelixlabs.axelix.master.service.convert.response.env;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.response.env.DangerousProperty;
import com.axelixlabs.axelix.master.api.external.response.env.EnvironmentFeedResponse;
import com.axelixlabs.axelix.master.contract.env.Deprecation;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.contract.env.InjectionPoint;
import com.axelixlabs.axelix.master.contract.env.Property;
import com.axelixlabs.axelix.master.contract.env.PropertySource;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.env.DangerousPropertyDetector;

/**
 * The {@link Converter} from {@link EnvironmentFeed} to {@link EnvironmentFeedResponse}. The dangerous values are
 * resolved by {@link DangerousPropertyDetector}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class EnvironmentFeedConverter implements Converter<EnvironmentFeed, EnvironmentFeedResponse> {

    private final DangerousPropertyDetector dangerousPropertyDetector;

    public EnvironmentFeedConverter(DangerousPropertyDetector dangerousPropertyDetector) {
        this.dangerousPropertyDetector = dangerousPropertyDetector;
    }

    @Override
    public @NonNull EnvironmentFeedResponse convertInternal(@NonNull EnvironmentFeed source) {
        Map<Property, DangerousProperty> dangerousProperties = dangerousPropertyDetector.detect(source);
        List<EnvironmentFeedResponse.PropertySource> propertySources = new ArrayList<>();

        for (PropertySource propertySource : source.getPropertySources()) {
            List<EnvironmentFeedResponse.Property> properties = new ArrayList<>();

            for (Property property : propertySource.getProperties()) {
                properties.add(new EnvironmentFeedResponse.Property(
                        property.getName(),
                        property.getValue(),
                        property.getIsPrimary(),
                        property.getConfigPropsBeanName(),
                        property.getDescription(),
                        convertDeprecation(property.getDeprecation()),
                        convertInjectionPoints(property.getInjectionPoints()),
                        convertDangerousValue(dangerousProperties.get(property))));
            }

            propertySources.add(new EnvironmentFeedResponse.PropertySource(
                    propertySource.getName(), propertySource.getDescription(), properties));
        }

        return new EnvironmentFeedResponse(source.getActiveProfiles(), source.getDefaultProfiles(), propertySources);
    }

    private EnvironmentFeedResponse.@Nullable DangerousValue convertDangerousValue(
            @Nullable DangerousProperty dangerousProperty) {
        return dangerousProperty == null
                ? null
                : new EnvironmentFeedResponse.DangerousValue(
                        dangerousProperty.getRationale(), dangerousProperty.getAlternativeExample());
    }

    private EnvironmentFeedResponse.@Nullable Deprecation convertDeprecation(@Nullable Deprecation deprecation) {
        return deprecation == null
                ? null
                : new EnvironmentFeedResponse.Deprecation(
                        deprecation.getMessage(), deprecation.getLevel(), deprecation.getReplacedBy());
    }

    private @Nullable List<EnvironmentFeedResponse.InjectionPoint> convertInjectionPoints(
            @Nullable List<InjectionPoint> injectionPoints) {
        if (injectionPoints == null) {
            return null;
        }

        return injectionPoints.stream()
                .map(injectionPoint -> new EnvironmentFeedResponse.InjectionPoint(
                        injectionPoint.getBeanName(),
                        injectionPoint.getInjectionType(),
                        injectionPoint.getTargetName(),
                        injectionPoint.getPropertyExpression()))
                .toList();
    }
}
