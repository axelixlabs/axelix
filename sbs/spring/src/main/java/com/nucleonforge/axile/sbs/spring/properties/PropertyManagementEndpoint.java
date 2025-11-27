package com.nucleonforge.axile.sbs.spring.properties;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(PropertyManagementEndpoint.class);

    private final PropertyMutator propertyMutator;

    private final PropertyNameDiscoverer propertyNameDiscoverer;

    public PropertyManagementEndpoint(PropertyMutator propertyMutator, PropertyNameDiscoverer propertyNameDiscoverer) {
        this.propertyMutator = propertyMutator;
        this.propertyNameDiscoverer = propertyNameDiscoverer;
    }

    @PostMapping
    public ResponseEntity<Void> mutate(@RequestBody PropertyMutationRequest request) {
        String propertyName = request.propertyName();

        if (propertyName == null || propertyName.isBlank()) {
            log.warn("Received property mutation request with blank/empty/null property name");
            return ResponseEntity.badRequest().build();
        }

        String discoveredPropertyName = propertyNameDiscoverer.discover(propertyName);

        propertyMutator.mutate(Objects.requireNonNullElse(discoveredPropertyName, propertyName), request.newValue());

        return ResponseEntity.noContent().build();
    }
}
