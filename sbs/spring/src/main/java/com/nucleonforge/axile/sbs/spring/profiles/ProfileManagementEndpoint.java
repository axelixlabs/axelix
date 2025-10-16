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
