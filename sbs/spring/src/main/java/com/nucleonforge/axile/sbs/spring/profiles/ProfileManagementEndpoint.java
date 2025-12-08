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
package com.nucleonforge.axile.sbs.spring.profiles;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes an operation for replacing active application profiles at runtime.
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "profile-management")
public class ProfileManagementEndpoint {

    private final ProfileMutator profileMutator;

    public ProfileManagementEndpoint(ProfileMutator profileMutator) {
        this.profileMutator = profileMutator;
    }

    @PostMapping
    public ProfileMutationResponse replaceProfiles(@RequestBody ProfileMutationRequest request) {
        String[] effectiveProfiles = (request == null || request.effectiveProfiles() == null)
                ? new String[0]
                : request.effectiveProfiles().toArray(new String[0]);

        return profileMutator.replaceActiveProfiles(effectiveProfiles);
    }
}
