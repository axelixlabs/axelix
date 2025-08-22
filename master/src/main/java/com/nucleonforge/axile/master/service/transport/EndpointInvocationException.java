package com.nucleonforge.axile.master.service.transport;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The exception that occurs when Axile Master tried to reach a particular {@link ActuatorEndpoint}
 * on the managed service, but the managed service is either not available, or responded with non 2xx status
 * code.
 *
 * @author Mikhail Polivakha
 */
public class EndpointInvocationException extends RuntimeException {

    public EndpointInvocationException(Throwable cause) {
        super(cause);
    }

    public EndpointInvocationException(String message) {
        super(message);
    }
}
