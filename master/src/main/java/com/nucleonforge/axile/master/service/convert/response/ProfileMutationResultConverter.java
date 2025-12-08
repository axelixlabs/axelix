/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
