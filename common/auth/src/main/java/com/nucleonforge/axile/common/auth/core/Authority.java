package com.nucleonforge.axile.common.auth.core;

/**
 * The SPI interface that represents an authority of a given user.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface Authority {

    /**
     * Name of the authority
     */
    String getName();
}
