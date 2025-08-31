package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

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
     * @return discovered {@link Property}, or {@code null} if no property with the given name is not found
     */
    @Nullable
    Property discover(String propertyName);
}
