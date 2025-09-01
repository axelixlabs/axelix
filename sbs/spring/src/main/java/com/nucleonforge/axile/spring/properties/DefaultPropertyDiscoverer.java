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

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource.containsProperty(propertyName)) {
                if (property == null) {
                    property = new Property(propertyName);

                    // TODO:
                    //  Is this correct? I mean, if the given property source contains the
                    //  property, it does not mean that this property source is a 'provider source',
                    //  see javadoc of Property
                    property.setProviderSource(propertySource);

                    Object value = propertySource.getProperty(propertyName);
                    property.setValue(value != null ? value.toString() : null);
                }
                property.addHoldingPropertySource(propertySource);
            }
        }

        return property;
    }
}
