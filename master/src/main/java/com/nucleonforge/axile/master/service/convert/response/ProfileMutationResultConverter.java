package com.nucleonforge.axile.master.service.convert.response;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.master.api.response.ProfileUpdateResponse;

/**
 * The {@link Converter} from {@link ProfileMutationResult} to {@link ProfileUpdateResponse}
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Service
public class ProfileMutationResultConverter implements Converter<ProfileMutationResult, ProfileUpdateResponse> {

    @Override
    public @NonNull ProfileUpdateResponse convertInternal(@NonNull ProfileMutationResult profileMutationResult) {
        return new ProfileUpdateResponse(profileMutationResult.updated(), profileMutationResult.reason());
    }
}
