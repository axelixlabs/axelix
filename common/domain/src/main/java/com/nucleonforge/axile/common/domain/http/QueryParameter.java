package com.nucleonforge.axile.common.domain.http;

/**
 * The HTTP query parameter.
 *
 * @param <T> the type of the parameter value
 * @author Mikhail Polivakha
 */
public sealed interface QueryParameter<T> permits SingleValueQueryParameter {

    /**
     * @return the key under which the parameter resides
     */
    String key();

    /**
     * @return the value of the query parameter
     */
    T value();

    /**
     * @return the String representation of {@link #value()}
     */
    default String asString() {
        return key() + "=" + value();
    }
}
