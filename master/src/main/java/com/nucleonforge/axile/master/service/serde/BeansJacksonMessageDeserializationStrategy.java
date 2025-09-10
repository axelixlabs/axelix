package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.BeansFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link BeansFeed}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class BeansJacksonMessageDeserializationStrategy extends JacksonMessageDeserializationStrategy<BeansFeed> {

    public BeansJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<BeansFeed> supported() {
        return BeansFeed.class;
    }
}
