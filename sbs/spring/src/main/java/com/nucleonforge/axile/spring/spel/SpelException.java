package com.nucleonforge.axile.spring.spel;

/**
 * An unchecked exception thrown when a Spring Expression Language (SpEL) expression
 * cannot be parsed or evaluated.
 *
 * @since 08.08.2025
 * @author Nikita Kirillov
 */
public class SpelException extends RuntimeException {

    public SpelException(final String message) {
        super(message);
    }

    public SpelException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
