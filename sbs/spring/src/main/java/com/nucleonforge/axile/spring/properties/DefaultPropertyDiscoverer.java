package com.nucleonforge.axile.spring.properties;

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
    public Property discover(String propertyName) {

        Property property = new Property(propertyName);

        MutablePropertySources propertySources = environment.getPropertySources();

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource.containsProperty(propertyName)) {

                property.addHoldingPropertySource(propertySource);

                if (property.getProviderSource() == null) {
                    property.setProviderSource(propertySource);

                    Object value = propertySource.getProperty(propertyName);
                    property.setValue(value != null ? value.toString() : null);
                }
            }
        }

        return property;
    }
}
