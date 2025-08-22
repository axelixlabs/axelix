package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link BeansFeedResponse}.
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
