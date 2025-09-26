package com.nucleonforge.axile.master.api.request;

import java.util.List;

/**
 * Represents a request to update the currently active Spring profiles
 * in a specific application instance.
 *
 * @param effectiveProfiles the list of profiles to activate in the targeted instance. Passing an empty
 * list will deactivate all currently active profiles.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileUpdatedRequest(List<String> effectiveProfiles) {}
