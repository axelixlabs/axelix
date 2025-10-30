package com.nucleonforge.axile.sbs.spring.details;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.nucleonforge.axile.common.api.details.AxileDetails;

/**
 * Custom Spring Boot Actuator endpoint. Provides comprehensive instance operational details.
 *
 * @see AxileDetails
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axile-details")
public class AxileDetailsEndpoint {

    private final ServiceDetailsAssembler serviceDetailsAssembler;

    public AxileDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
        this.serviceDetailsAssembler = serviceDetailsAssembler;
    }

    @ReadOperation
    public AxileDetails details() {
        return serviceDetailsAssembler.assemble();
    }
}
