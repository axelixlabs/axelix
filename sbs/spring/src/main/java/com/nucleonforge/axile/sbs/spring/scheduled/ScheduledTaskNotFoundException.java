package com.nucleonforge.axile.sbs.spring.scheduled;

/**
 * An unchecked exception thrown when a requested scheduled task cannot be found
 * in the task registry.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public class ScheduledTaskNotFoundException extends RuntimeException {
    public ScheduledTaskNotFoundException(String message) {
        super(message);
    }
}
