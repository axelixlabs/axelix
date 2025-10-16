package com.nucleonforge.axile.sbs.spring.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;

/**
 * Custom Spring Boot Actuator endpoint. Provides access to basic build information
 * such as the application version.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axile-metadata")
public class AxileMetadataEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AxileMetadataEndpoint.class);

    private final ServiceMetadataAssembler serviceMetadataAssembler;

    public AxileMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        this.serviceMetadataAssembler = serviceMetadataAssembler;
    }

    @ReadOperation
    public ServiceMetadata getMetadata() {
        log.debug("Received request for metadata assembly");

        ServiceMetadata serviceMetadata = serviceMetadataAssembler.assemble();

        log.debug("Assembled metadata for this service : {}", serviceMetadata);

        return serviceMetadata;
    }
}
