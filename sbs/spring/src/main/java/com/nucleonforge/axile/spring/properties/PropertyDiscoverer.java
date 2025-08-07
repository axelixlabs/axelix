package com.nucleonforge.axile.spring.properties;

/**
 * The interface that can discover the {@link Property properties} by their names
 *
 * @since 07.04.25
 * @author Mikhail Polivakha
 */
public interface PropertyDiscoverer {

    /**
     * Actual discovery method
     *
     * @param propertyName the name of the property to be discovered
     * @return discovered {@link Property}
     */
    Property discover(String propertyName);
}
