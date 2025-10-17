package com.nucleonforge.axile.sbs.spring.profiles;

import org.springframework.context.ApplicationContext;

/**
 * Interface responsible for replacing the active Spring profiles at runtime.
 * <p>
 * Implementations must ensure that any profile modification is properly applied by
 * updating the environment and restarting the {@link ApplicationContext}
 * to reflect the changes across the application.
 * </p>
 *
 * @author Nikita Kirillov
 * @since 11.07.2025
 */
public interface ProfileMutator {

    /**
     * Replaces the current set of active profiles with the provided array.
     * <p>
     * All existing active profiles will be removed, and only the specified ones will be activated.
     * </p>
     *
     * @param profiles the new set of profiles to activate
     * @return the result of the profile replacement operation
     */
    ProfileMutationResponse replaceActiveProfiles(String[] profiles);
}
