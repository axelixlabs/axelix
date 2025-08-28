package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import com.nucleonforge.axile.spring.properties.PropertySourceDescription.PropertySourceOrigin;
import com.nucleonforge.axile.spring.utils.FieldUtils;

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
