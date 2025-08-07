package com.nucleonforge.axile.spring.properties;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.NonNull;

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
    public MutationResponse mutate(@Selector @NonNull String propertyName, String newValue) {
        if (propertyName.isBlank()) {
            return new MutationResponse(false, "Property name is required");
        }

        Property property = propertyDiscoverer.discover(propertyName);
        return propertyMutator.mutate(property, newValue);
    }
}
