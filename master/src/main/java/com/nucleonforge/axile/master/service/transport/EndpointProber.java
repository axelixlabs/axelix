package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.instance.InstanceId;

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
     * @param instanceId the id of the instance on which the endpoint should be invoked.
     * @param httpPayload the abstraction that encapsulates the http payload of the request
     * @return the result of the invocation. Guaranteed to be not null.
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     * @throws InstanceNotFoundException in case the instance with the given ID is not present.
     */
    @NonNull
    O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException;

    /**
     * Invoke the actual {@link ActuatorEndpoint} using the given base url.
     *
     * @param baseUrl the base url of the request
     * @param httpPayload the abstraction that encapsulates the http payload of the request
     * @return the result of the invocation. Guaranteed to be not null.
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     */
    @NonNull
    O invoke(@NonNull String baseUrl, HttpPayload httpPayload) throws EndpointInvocationException;

    /**
     * @return the {@link ActuatorEndpoint} that this prober supports
     */
    @NonNull
    ActuatorEndpoint supports();
}
