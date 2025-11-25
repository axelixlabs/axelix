package com.nucleonforge.axile.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * Interface for discovering the property name stored in the {@link ConfigurableEnvironment}.
 * <p>
 * If the property name exists in both {@link SystemEnvironmentPropertySource}
 * and {@link PropertiesPropertySource}, the name will be taken
 * from the source with the highest priority
 *
 * @author Sergey Cherkasov
 */
public interface PropertyNameDiscoverer {

    /**
     * Discovers the primary property name.
     *
     * @param propertyName property name to be discovered
     * @return discovered {@code propertyName}, or {@code null} if no property with the given name is not found
     */
    @Nullable
    String discover(String propertyName);
}
