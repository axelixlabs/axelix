package com.nucleonforge.axile.master.api.error;

import org.jspecify.annotations.NonNull;

/**
 * Interface for an error to be sent from the master backend to the front-end app.
 *
 * @author Mikhail Polivakha
 */
public interface ApiError {

    /**
     * @return Code of the error. Guaranteed to be not null.
     */
    @NonNull
    String code();
    //
    //    /**
    //     * Any possible additional parameters that may communicate some context about
    //     * the error that happened. This {@link Map} cannot be null, but it can easily
    //     * be empty in case backend does not consider to send any additional parameters.
    //     * This {@link Map} may contain some internal complex structures, such as Other
    //     * {@link Map maps} for intance.
    //     */
    //    @NonNull Map<String, Object> params();
}
