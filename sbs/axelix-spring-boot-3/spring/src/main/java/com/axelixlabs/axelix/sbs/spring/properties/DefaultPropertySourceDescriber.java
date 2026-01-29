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

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import com.axelixlabs.axelix.sbs.spring.properties.PropertySourceDescription.PropertySourceOrigin;
import com.axelixlabs.axelix.sbs.spring.utils.FieldUtils;

/**
 * Default {@link PropertySourceDescriber}.
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public class DefaultPropertySourceDescriber implements PropertySourceDescriber {

    @Override
    public PropertySourceDescription describe(PropertySource<?> propertySource) {

        PropertySourceOrigin origin = determineOrigin(propertySource);

        String propertiesFileName = determineFileName(origin, propertySource);

        return new PropertySourceDescription(
                propertySource.getName(), origin, propertySource.getClass(), propertiesFileName);
    }

    @Nullable
    private String determineFileName(PropertySourceOrigin origin, PropertySource<?> propertySource) {
        if (PropertySourceOrigin.PROPERTIES_FILE.equals(origin)
                && propertySource instanceof ResourcePropertySource rps) {
            return FieldUtils.getField("resourceName", rps);
        } else {
            return null;
        }
    }

    private static PropertySourceOrigin determineOrigin(PropertySource<?> propertySource) {
        if (propertySource instanceof SystemEnvironmentPropertySource s) {
            if (StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(s.getName())) {
                return PropertySourceOrigin.ENVIRONMENT_VARIABLES;
            } else {
                return PropertySourceOrigin.CUSTOM;
            }
        } else if (propertySource instanceof ResourcePropertySource) {
            return PropertySourceOrigin.PROPERTIES_FILE;
        } else if (propertySource instanceof PropertiesPropertySource s) {
            if (StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME.equals(s.getName())) {
                return PropertySourceOrigin.SYSTEM_ARGS;
            } else {
                return PropertySourceOrigin.CUSTOM;
            }
        } else {
            return PropertySourceOrigin.CUSTOM;
        }
    }
}
