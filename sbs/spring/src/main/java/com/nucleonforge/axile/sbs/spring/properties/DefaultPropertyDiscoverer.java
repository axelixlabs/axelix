package com.nucleonforge.axile.sbs.spring.properties;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import com.nucleonforge.axile.sbs.spring.env.EnvironmentPropertyNameNormalizer;

/**
 * Default {@link PropertyDiscoverer}. Looks up property by inspecting the {@link Environment}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultPropertyDiscoverer implements PropertyDiscoverer {

    private final Environment environment;
    private final EnvironmentPropertyNameNormalizer propertyNameNormalizer;

    public DefaultPropertyDiscoverer(
            Environment environment, EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
        this.environment = environment;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    @Nullable
    public String discover(String propertyName) {
        Map<String, String> envPropsMapping = buildConfigEnvMappingMap();

        return envPropsMapping.getOrDefault(propertyNameNormalizer.normalize(propertyName), null);
    }

    private Map<String, String> buildConfigEnvMappingMap() {
        Map<String, String> propertyNames = new HashMap<>();
        for (PropertySource<?> source : getPropertySources()) {
            extractPropertyNames(source, propertyNames);
        }
        return propertyNames;
    }

    private void extractPropertyNames(PropertySource<?> source, Map<String, String> propertyNames) {
        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                extractPropertyNames(nest, propertyNames);
            }
        } else if (source instanceof EnumerablePropertySource<?> enumerable) {
            for (String name : enumerable.getPropertyNames()) {
                propertyNames.put(propertyNameNormalizer.normalize(name), name);
            }
        }
    }

    private MutablePropertySources getPropertySources() {
        if (this.environment instanceof ConfigurableEnvironment configurableEnvironment) {
            return configurableEnvironment.getPropertySources();
        }
        return new StandardEnvironment().getPropertySources();
    }
}
