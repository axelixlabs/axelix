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
package com.axelixlabs.axelix.sbs.spring.properties;

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
