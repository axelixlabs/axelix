package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.ProfileMutationResult;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link BeansFeed}.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Component
public class ProfileMutationJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ProfileMutationResult> {

    public ProfileMutationJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ProfileMutationResult> supported() {
        return ProfileMutationResult.class;
    }
}
