package com.nucleonforge.axile.spring.properties;

import org.springframework.context.ApplicationContext;

/**
 * The interface that is capable to modify the {@link Property} value.
 * <p>
 * The implementations must reload all necessary {@link ApplicationContext} components that rely on this property.
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public interface PropertyMutator {

    /**
     * Mutate the property
     *
     * @param propertyName the property name to be mutated
     * @param newValue the new value of the property
     */
    void mutate(String propertyName, String newValue);
}
