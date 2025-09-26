package com.nucleonforge.axile.spring.properties;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing application properties at runtime.
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "property-management")
public class PropertyManagementEndpoint {

    private final PropertyDiscoverer propertyDiscoverer;
    private final PropertyMutator propertyMutator;

    public PropertyManagementEndpoint(PropertyDiscoverer propertyDiscoverer, PropertyMutator propertyMutator) {
        this.propertyDiscoverer = propertyDiscoverer;
        this.propertyMutator = propertyMutator;
    }

    @PostMapping
    public ResponseEntity<Void> mutate(@RequestBody PropertyMutationRequest request) {
        Property property = propertyDiscoverer.discover(request.propertyName());

        if (property == null) {
            throw new PropertyNotFoundException("Property '" + request.propertyName() + "' not found");
        }
        propertyMutator.mutate(property, request.newValue());
        return ResponseEntity.noContent().build();
    }
}
