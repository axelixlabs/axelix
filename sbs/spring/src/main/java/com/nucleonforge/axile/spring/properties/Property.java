package com.nucleonforge.axile.spring.properties;

import java.util.HashSet;
import java.util.Set;

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
    private String value;

    /**
     * Spring's {@link PropertySource PropertySources} that contain this property.
     */
    private Set<PropertySource<?>> holdingPropertySources;

    /**
     * Spring's {@link PropertySource} that won, meaning, the property source from which
     * the property is actually derived at runtime
     */
    private PropertySource<?> providerSource;

    public Property(String name) {
        this.name = name;
    }

    public void addHoldingPropertySource(PropertySource<?> propertySource) {
        if (holdingPropertySources == null) {
            holdingPropertySources = new HashSet<>();
        }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }
}
