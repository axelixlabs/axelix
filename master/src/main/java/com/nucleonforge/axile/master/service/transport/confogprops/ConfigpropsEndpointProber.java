package com.nucleonforge.axile.master.service.transport.confogprops;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#CONFIG_PROPS /configprops} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigpropsEndpointProber extends AbstractEndpointProber<ConfigpropsFeed> {

    public ConfigpropsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ConfigpropsFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.CONFIG_PROPS;
    }
}
