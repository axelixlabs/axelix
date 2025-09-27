package com.nucleonforge.axile.master.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.nucleonforge.axile.common.api.ProfileMutationResult;

/**
 * The response of a profile update operation in the application.
 *
 * @param updated indicates whether the profiles were successfully updated
 * @param reason  the reason describing why the update was applied or skipped
 *
 * @see ProfileMutationResult
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileUpdateResponse(
        @Schema(description = "Indicates whether the profiles were successfully updated", example = "true")
                boolean updated,
        @Schema(
                        description = "The reason describing why the update was applied or skipped (may be null)",
                        nullable = true,
                        example = "Profiles replaced successfully")
                String reason) {}
