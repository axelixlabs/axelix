package com.nucleonforge.axile.master.api.response;

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
public record ProfileUpdateResponse(boolean updated, String reason) {}
