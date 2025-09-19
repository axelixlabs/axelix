package com.nucleonforge.axile.spring.master;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Custom Spring Boot Actuator endpoint. Provides access to basic build information
 * such as the application group and version.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axile-metadata")
public class AxileMetadataEndpoint {

    @ReadOperation // TODO currently hardcoded - is ok.
    public AxileMetadataResponse getMetadata() {
        return new AxileMetadataResponse("com.nucleonforge.axile", "1.0.0-SNAPSHOT");
    }
}
