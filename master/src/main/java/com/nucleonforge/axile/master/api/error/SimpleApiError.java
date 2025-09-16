package com.nucleonforge.axile.master.api.error;

import org.jspecify.annotations.NonNull;

/**
 * Simple implementation of the {@link ApiError} with no additional information.
 *
 * @author Mikhail Polivakha
 */
public record SimpleApiError(@NonNull String code) implements ApiError {}
