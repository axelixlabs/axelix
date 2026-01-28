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
package com.axelixlabs.axelix.sbs.spring.configprops;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;

import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

/**
 * {@link SanitizingFunction} that is capable to make sanitization decisions on a per-property basis.
 *
 * <p>Typically, only the specific specified properties are sanitized, see {@link #toBeSanitized}. However,
 * there is a universal placeholder {@link EndpointsConfigurationProperties#SANITIZE_ALL} which implies that
 * all properties values must be sanitized.
 *
 * @author Mikhail Polivakha
 */
public class SmartSanitizingFunction implements SanitizingFunction {

    private final HashSet<String> toBeSanitized;
    private final UnaryOperator<@Nullable String> sanitizationFunction;
    private final PropertyNameNormalizer propertyNameNormalizer;
    private final boolean shouldSanitizeAllValues;

    /**
     * @param toBeSanitized names of properties to be sanitized
     */
    public SmartSanitizingFunction(List<String> toBeSanitized, PropertyNameNormalizer propertyNameNormalizer) {
        this.shouldSanitizeAllValues = Objects.equals(toBeSanitized, EndpointsConfigurationProperties.SANITIZE_ALL);
        this.toBeSanitized = new HashSet<>(toBeSanitized.size(), 1.1f);

        for (String propertyName : toBeSanitized) {
            String normalizedProperty = propertyNameNormalizer.normalize(propertyName);
            this.toBeSanitized.add(normalizedProperty);
        }

        this.sanitizationFunction = (@Nullable String propertyValue) -> "******";
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    public SanitizableData apply(SanitizableData data) {

        Object finalValue = data.getValue();

        if (shouldSanitizeAllValues) {
            finalValue = getSanitizedValue(data);
        } else {
            String normalized = propertyNameNormalizer.normalize(data.getKey());

            if (toBeSanitized.contains(normalized)) {
                finalValue = getSanitizedValue(data);
            }
        }

        return new SanitizableData(data.getPropertySource(), data.getKey(), finalValue);
    }

    private @Nullable Object getSanitizedValue(SanitizableData data) {
        String stringValueOrNull =
                Optional.ofNullable(data.getValue()).map(Object::toString).orElse(null);

        return sanitizationFunction.apply(stringValueOrNull);
    }
}
