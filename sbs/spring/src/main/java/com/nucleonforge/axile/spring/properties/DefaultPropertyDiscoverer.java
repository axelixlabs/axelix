package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

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

        MutablePropertySources propertySources = environment.getPropertySources();
        Property property = null;

        // We rely on the ordering of PropertySources: the first source containing the property
        // is chosen as providerSource
        for (PropertySource<?> propertySource : propertySources) {
            if (!propertySource.containsProperty(propertyName)) {
                continue;
            }

            if (property == null) {
                property = new Property(propertyName);
                Object value = propertySource.getProperty(propertyName);
                property.setValue(value != null ? value.toString() : null);
                if (value != null) {
                    property.setProviderSource(propertySource);
                }
            }

            property.addHoldingPropertySource(propertySource);
        }

        return property;
    }
}
