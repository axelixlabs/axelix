package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.api.AxileMetadata;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * This interface defines the contract for fetching {@link AxileMetadata} from a service's actuator endpoint.
 * <p>
 *
 * @since 19.08.2025
 * @author Nikita Kirillov
 */
public interface MetadataEndpointProber {

    /**
     * Invokes the metadata endpoint of the given service instance.
     *
     * @param instanceActuatorUrl the base actuator URL of the service instance, including "/actuator"
     * @return the {@link AxileMetadata} retrieved from the service instance, never null
     * @throws EndpointInvocationException if the request to the instance fails or returns a non-2xx status
     */
    @NonNull
    AxileMetadata invoke(@NonNull String instanceActuatorUrl) throws EndpointInvocationException;

    /**
     * @return the {@link ActuatorEndpoint} that this prober is capable of invoking.
     */
    @NonNull
    ActuatorEndpoint supports();
}
