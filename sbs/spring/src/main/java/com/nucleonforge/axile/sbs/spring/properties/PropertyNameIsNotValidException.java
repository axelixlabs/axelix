package com.nucleonforge.axile.sbs.spring.properties;

/**
 * An unchecked exception thrown when a requested property has an invalid name.
 *
 * @since 13.10.2025
 * @author Sergey Cherkasov
 */
public class PropertyNameIsNotValidException extends RuntimeException {

    public PropertyNameIsNotValidException(String message) {
        super(message);
    }
}
