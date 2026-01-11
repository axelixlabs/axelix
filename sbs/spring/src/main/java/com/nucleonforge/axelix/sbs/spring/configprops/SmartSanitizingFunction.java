/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.configprops;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;

import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;

/**
 * {@link SanitizingFunction} that is capable to make sanitization decisions on a per-property basis.
 * <p/>
 * Typically, only the specific specified properties are sanitized, see {@link #toBeSanitized}. However,
 * there is a universal placeholder {@link ConfigPropsConfigurationProperties#SANITIZE_ALL} which implies that
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
        this.shouldSanitizeAllValues = Objects.equals(toBeSanitized, ConfigPropsConfigurationProperties.SANITIZE_ALL);
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
