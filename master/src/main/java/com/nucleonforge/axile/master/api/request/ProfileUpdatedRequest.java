package com.nucleonforge.axile.master.api.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

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
public record ProfileUpdatedRequest(
        @ArraySchema(
                        schema = @Schema(example = "[\"profile1\", \"profile2\"]", type = "array"),
                        arraySchema =
                                @Schema(
                                        description =
                                                "Array of effective profiles. To remove all active profiles in instance, the array must be empty"))
                List<String> effectiveProfiles) {}
