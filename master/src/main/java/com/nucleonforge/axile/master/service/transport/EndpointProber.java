package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;

/**
 * The core service that is responsible to probe certain information from discovered services.
 * <p>
 *
 * @param <O> the type of the response body (output).
 * @author Mikhail Polivakha
 */
public interface EndpointProber<O> {

    /**
     * Invoke the actual {@link ActuatorEndpoint} on the managed service.
     *
     * @param instanceId  the id of the instance on which the endpoint should be invoked.
     * @param httpPayload
     * @return the result of the invocation. Guaranteed to be not null.
     * @throws InstanceNotFoundException in case the invocation to managed service did not result in successful response.
     * @throws InstanceNotFoundException in case the instance with the given ID is not present.
     */
    @NonNull
    O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException;

    /**
     * @return the {@link ActuatorEndpoint} that this prober supports
     */
    @NonNull
    ActuatorEndpoint supports();
}
