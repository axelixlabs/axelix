package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.InstanceReference;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * Typically thrown by the {@link InstanceRegistry} in case the given {@link InstanceReference}
 * is not found.
 *
 * @author Mikhail Polivakha
 */
public class InstanceNotFoundException extends RuntimeException {

    public InstanceNotFoundException() {}

    public InstanceNotFoundException(InstanceId instanceId) {
        super("The application's instance with id '%s' is not found".formatted(instanceId));
    }
}
