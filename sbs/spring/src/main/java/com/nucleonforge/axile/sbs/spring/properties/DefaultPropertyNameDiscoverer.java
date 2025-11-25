package com.nucleonforge.axile.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.nucleonforge.axile.sbs.spring.env.EnvironmentPropertyNameNormalizer;

/**
 * Default {@link PropertyNameDiscoverer}. Looks up property name by inspecting the {@link ConfigurableEnvironment}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameDiscoverer implements PropertyNameDiscoverer {

    private final ConfigurableEnvironment configurableEnvironment;
    private final EnvironmentPropertyNameNormalizer propertyNameNormalizer;

    public DefaultPropertyNameDiscoverer(
            ConfigurableEnvironment configurableEnvironment, EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    @Nullable
    public String discover(String propertyName) {

        for (PropertySource<?> source : configurableEnvironment.getPropertySources()) {
            String foundPropertyName = extractPropertyNames(source, propertyNameNormalizer.normalize(propertyName));
            if (foundPropertyName != null) {
                return foundPropertyName;
            }
        }

        return null;
    }

    // We rely on the priority order of property sources in PropertySource,
    // from highest to lowest priority.
    private @Nullable String extractPropertyNames(PropertySource<?> source, String normalizedPropertyName) {

        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                String foundPropertyName = extractPropertyNames(nest, normalizedPropertyName);
                if (foundPropertyName != null) {
                    return foundPropertyName;
                }
            }
        } else if (source instanceof EnumerablePropertySource<?> enumerable) {
            for (String name : enumerable.getPropertyNames()) {
                if (normalizedPropertyName.equals(propertyNameNormalizer.normalize(name))) {
                    return name;
                }
            }
        }

        return null;
    }
}
