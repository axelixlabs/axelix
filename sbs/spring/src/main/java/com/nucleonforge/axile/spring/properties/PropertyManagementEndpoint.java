package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing application properties at runtime.
 *
 * <p>This endpoint delegates property discovery and mutation operations to the
 * {@link PropertyDiscoverer} and {@link PropertyMutator} implementations.</p>
 *
 * <p>All operations are exposed via HTTP POST requests under the {@code /actuator/property-management} path.</p>
 *
 * <p>Supported operation:</p>
 * <ul>
 *     <li>{@code mutate(propertyName, newValue)} — updates the specified property to a new value.</li>
 * </ul>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "property-management")
public class PropertyManagementEndpoint {

    private final PropertyDiscoverer propertyDiscoverer;
    private final PropertyMutator propertyMutator;

    public PropertyManagementEndpoint(PropertyDiscoverer propertyDiscoverer, PropertyMutator propertyMutator) {
        this.propertyDiscoverer = propertyDiscoverer;
        this.propertyMutator = propertyMutator;
    }

    @WriteOperation
    public void mutate(@Selector @NonNull String propertyName, String newValue) {
        Property property = propertyDiscoverer.discover(propertyName);

        if (property == null) {
            throw new PropertyNotFoundException("Property '" + propertyName + "' not found");
        }
        propertyMutator.mutate(property, newValue);
    }
}
