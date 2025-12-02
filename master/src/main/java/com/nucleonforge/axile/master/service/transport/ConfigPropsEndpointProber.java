package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#CONFIG_PROPS /axile-configprops} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigPropsEndpointProber extends AbstractEndpointProber<ConfigPropsFeed> {

    public ConfigPropsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ConfigPropsFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.CONFIG_PROPS;
    }
}
