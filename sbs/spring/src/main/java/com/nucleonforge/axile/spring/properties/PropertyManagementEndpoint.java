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

    private final PropertyMutator propertyMutator;

    public PropertyManagementEndpoint(PropertyMutator propertyMutator) {
        this.propertyMutator = propertyMutator;
    }

    @PostMapping
    public ResponseEntity<Void> mutate(@RequestBody PropertyMutationRequest request) {
        String propertyName = request.propertyName();
        if (propertyName == null || propertyName.isBlank()) {
            throw new PropertyNameIsNotValidException("Property name '" + propertyName + "' is not valid.");
        }

        propertyMutator.mutate(propertyName, request.newValue());
        return ResponseEntity.noContent().build();
    }
}
