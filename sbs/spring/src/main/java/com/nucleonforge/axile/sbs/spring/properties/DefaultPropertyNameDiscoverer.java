/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.nucleonforge.axile.sbs.spring.env.PropertyNameNormalizer;

/**
 * Default {@link PropertyNameDiscoverer}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameDiscoverer implements PropertyNameDiscoverer {

    private final ConfigurableEnvironment configurableEnvironment;
    private final PropertyNameNormalizer propertyNameNormalizer;

    public DefaultPropertyNameDiscoverer(
            ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    @Nullable
    public String discover(String propertyName) {

        for (PropertySource<?> source : configurableEnvironment.getPropertySources()) {
            String foundPropertyName = extractPropertyNames(source, propertyNameNormalizer.normalize(propertyName));
            if (foundPropertyName != null) {
                return foundPropertyName;
            }
        }

        return null;
    }

    // We rely on the priority order of property sources in PropertySource,
    // from highest to lowest priority.
    private @Nullable String extractPropertyNames(PropertySource<?> source, String normalizedPropertyName) {

        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                String foundPropertyName = extractPropertyNames(nest, normalizedPropertyName);
                if (foundPropertyName != null) {
                    return foundPropertyName;
                }
            }
        } else if (source instanceof EnumerablePropertySource<?> enumerable) {
            for (String name : enumerable.getPropertyNames()) {
                if (normalizedPropertyName.equals(propertyNameNormalizer.normalize(name))) {
                    return name;
                }
            }
        }

        return null;
    }
}
