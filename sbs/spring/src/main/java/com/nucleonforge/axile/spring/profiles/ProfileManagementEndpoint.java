package com.nucleonforge.axile.spring.profiles;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes an operation for replacing active application profiles at runtime.
 *
 * <p>This endpoint delegates the replacement logic to the {@link ProfileMutator} implementation.</p>
 *
 * <p>The operation is exposed via an HTTP POST request to the {@code /actuator/profile-management} path.</p>
 *
 * <p>Supported operation:</p>
 * <ul>
 *     <li>{@code replaceProfiles(profiles)} — replaces all currently active profiles with the provided list.</li>
 * </ul>
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "profile-management")
public class ProfileManagementEndpoint {

    private final ProfileMutator profileMutator;

    public ProfileManagementEndpoint(ProfileMutator profileMutator) {
        this.profileMutator = profileMutator;
    }

    @WriteOperation
    public ProfileMutationResponse replaceProfiles(@Selector String profiles) {
        if (profiles == null || profiles.isBlank()) {
            return profileMutator.replaceActiveProfiles(new String[0]);
        }

        return profileMutator.replaceActiveProfiles(profiles.split(","));
    }
}
