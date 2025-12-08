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
package com.nucleonforge.axile.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import com.nucleonforge.axile.sbs.spring.properties.PropertySourceDescription.PropertySourceOrigin;
import com.nucleonforge.axile.sbs.spring.utils.FieldUtils;

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
