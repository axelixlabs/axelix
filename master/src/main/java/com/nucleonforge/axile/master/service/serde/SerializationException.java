package com.nucleonforge.axile.master.service.serde;

/**
 * The exception that happens during serialization.
 *
 * @author Mikhail Polivakha
 */
public class SerializationException extends RuntimeException {

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
