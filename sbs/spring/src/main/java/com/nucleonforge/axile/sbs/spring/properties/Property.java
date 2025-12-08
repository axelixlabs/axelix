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

import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.PropertySource;

/**
 * The property of the spring application. Typically injected via {@link Value @Value} or other means
 * in the client applications
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public class Property {

    /**
     * Fully qualified name of the property, for instance {@literal spring.application.name}
     */
    private final String name;

    /**
     * String representation of the property's value
     */
    @Nullable
    private String value;

    /**
     * Spring's {@link PropertySource PropertySources} that contain this property.
     */
    private final Set<PropertySource<?>> holdingPropertySources = new HashSet<>();

    /**
     * Spring's {@link PropertySource} that won, meaning, the property source from which
     * the property is actually derived at runtime.
     * <p>
     * By design, {@link Property} is guaranteed to exist in at least one Spring
     * {@link PropertySource}, so this providerSource is never null.
     */
    @SuppressWarnings("NullAway")
    private PropertySource<?> providerSource;

    public Property(String name) {
        this.name = name;
    }

    public void addHoldingPropertySource(PropertySource<?> propertySource) {
        holdingPropertySources.add(propertySource);
    }

    public Set<PropertySource<?>> getHoldingPropertySources() {
        return holdingPropertySources;
    }

    public PropertySource<?> getProviderSource() {
        return providerSource;
    }

    public void setProviderSource(PropertySource<?> providerSource) {
        this.providerSource = providerSource;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }
}
