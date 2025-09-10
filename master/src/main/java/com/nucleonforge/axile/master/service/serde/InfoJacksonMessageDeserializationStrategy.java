package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.info.ServiceInfo;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ServiceInfo}.
 *
 * @author SergeyCherkasov
 */
@Component
public class InfoJacksonMessageDeserializationStrategy extends JacksonMessageDeserializationStrategy<ServiceInfo> {
    public InfoJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ServiceInfo> supported() {
        return ServiceInfo.class;
    }
}
