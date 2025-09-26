package com.nucleonforge.axile.master.exception;

/**
 * Runtime exception thrown when a profile mutation request cannot be serialized.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public class ProfileSerializationException extends RuntimeException {

    public ProfileSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
