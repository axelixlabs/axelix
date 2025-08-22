package com.nucleonforge.axile.master.service.serde;

/**
 * The exception that occurred during deserialization.
 *
 * @author Mikhail Polivakha
 */
public class DeserializationException extends RuntimeException {

    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
