package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * The core service that is responsible to probe certain information from discovered services.
 * <p>
 * This prober is capable to probe {@link #supports() supported actuator endpoint} and provide data/payload
 * in the request, as opposed to {@link EndpointProber}, which itself does not carry any request payload.
 *
 * @see PayloadCarryingEndpointProber
 * @param <O> the type of the response body (output).
 * @author Mikhail Polivakha
 */
public interface PayloadCarryingEndpointProber<I, O> {

    /**
     * Invoke the actual {@link ActuatorEndpoint} on the managed service.
     *
     * @param body the body of the given request. Must not be null.
     * @param instanceId the id of the instance on which the endpoint should be invoked.
     * @throws InstanceNotFoundException in case the invocation to managed service did not result in successful response.
     * @throws InstanceNotFoundException in case the instance with the given ID is not present.
     * @return the result of the invocation. Guaranteed to be not null.
     */
    @NonNull
    O invoke(@NonNull I body, InstanceId instanceId) throws EndpointInvocationException, InstanceNotFoundException;

    /**
     * @return the {@link ActuatorEndpoint} that this prober supports
     */
    @NonNull
    ActuatorEndpoint supports();
}
