package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.AxileDetails;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link AxileDetails}.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@Component
public class DetailsJacksonMessageDeserializationStrategy extends JacksonMessageDeserializationStrategy<AxileDetails> {
    public DetailsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<AxileDetails> supported() {
        return AxileDetails.class;
    }
}
