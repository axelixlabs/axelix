package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Default {@link PropertyDiscoverer}. Looks up {@link Property} by inspecting the {@link ConfigurableEnvironment}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public class DefaultPropertyDiscoverer implements PropertyDiscoverer {

    private final ConfigurableEnvironment environment;

    public DefaultPropertyDiscoverer(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    @Nullable
    public Property discover(String propertyName) {

        // TODO This is not what we ideally want and it will cause many issues,
        //  but as a temporary mechanism, we need to find an acceptable alternative.
        String normalizedPropertyName =
                !ConfigurationPropertyName.isValid(propertyName) ? normalizeAndValidate(propertyName) : propertyName;

        MutablePropertySources propertySources = environment.getPropertySources();
        Property property = null;

        // We rely on the ordering of PropertySources: the first source containing the property
        // is chosen as providerSource
        for (PropertySource<?> propertySource : propertySources) {
            if (!propertySource.containsProperty(normalizedPropertyName)) {
                continue;
            }

            if (property == null) {
                property = new Property(normalizedPropertyName);
                Object value = propertySource.getProperty(normalizedPropertyName);
                property.setValue(value != null ? value.toString() : null);
                if (value != null) {
                    property.setProviderSource(propertySource);
                }
            }

            property.addHoldingPropertySource(propertySource);
        }

        return property;
    }

    private String normalizeAndValidate(String propertyName) {
        String[] parts = propertyName.split("\\.");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
            parts[i] = parts[i].replaceAll("[^a-z0-9-]", "");
        }

        String normalizedPropertyName = String.join(".", parts);

        return ConfigurationPropertyName.isValid(normalizedPropertyName) ? normalizedPropertyName : propertyName;
    }
}
