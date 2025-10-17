package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.serde.NoOpMessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The {@link AbstractEndpointProber} that either do not return any value, or just discards the return value.
 *
 * @author Mikhail Polivakha
 */
public abstract class DiscardingAbstractEndpointProber extends AbstractEndpointProber<byte[]> {

    protected DiscardingAbstractEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry, NoOpMessageDeserializationStrategy.INSTANCE);
    }

    @Override
    public byte @NonNull [] invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {
        return super.invoke(instanceId, httpPayload);
    }

    public void invokeNoValue(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, InstanceNotFoundException {
        super.invoke(instanceId, httpPayload);
    }
}
