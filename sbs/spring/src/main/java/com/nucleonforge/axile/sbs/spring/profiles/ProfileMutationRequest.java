package com.nucleonforge.axile.sbs.spring.profiles;

import java.util.List;

/**
 * Represents a request to mutate (replace) the currently active Spring profiles
 * in the application.
 *
 * @param effectiveProfiles the list of profiles to activate. Passing an empty
 * list will deactivate all currently active profiles.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileMutationRequest(List<String> effectiveProfiles) {}
