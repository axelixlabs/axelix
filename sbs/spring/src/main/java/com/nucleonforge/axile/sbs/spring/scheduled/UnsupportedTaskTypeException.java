package com.nucleonforge.axile.sbs.spring.scheduled;

/**
 * An unchecked exception thrown when attempting to work with an unsupported
 * scheduled task type that cannot be properly managed by the system.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public class UnsupportedTaskTypeException extends RuntimeException {
    public UnsupportedTaskTypeException(String message) {
        super(message);
    }
}
