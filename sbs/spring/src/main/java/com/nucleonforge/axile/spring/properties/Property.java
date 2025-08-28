package com.nucleonforge.axile.spring.properties;

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
