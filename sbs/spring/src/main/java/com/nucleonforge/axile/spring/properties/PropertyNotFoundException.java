package com.nucleonforge.axile.spring.properties;

/**
 * An unchecked exception thrown when a requested property cannot be found
 * in any of the available {@link org.springframework.core.env.PropertySource}s.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }
}
