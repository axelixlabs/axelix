package com.nucleonforge.axile.spring.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.nucleonforge.axile.spring.context.ContextRestarter;

import static com.nucleonforge.axile.spring.properties.AxilePropertySource.AXILE_PROPERTY_SOURCE_NAME;

/**
 * {@link PropertyMutator} that reloads the {@link ApplicationContext} after property change so that the change
 * propagates everywhere where required.
 *
 * @since 07.04.2025
 * @author Mikhail Polivakha
 */
public class ContextReloadingPropertyMutator implements PropertyMutator {

    private final ConfigurableEnvironment configurableEnvironment;
    private final ContextRestarter contextRestarter;

    public ContextReloadingPropertyMutator(
            ConfigurableEnvironment configurableEnvironment, ContextRestarter contextRestarter) {
        this.configurableEnvironment = configurableEnvironment;
        this.contextRestarter = contextRestarter;
    }

    @Override
    public void mutate(Property property, String newValue) {
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        PropertySource<?> potentiallyAxilePropertySource = propertySources.get(AXILE_PROPERTY_SOURCE_NAME);

        if (potentiallyAxilePropertySource == null) {
            Map<String, Object> source = new HashMap<>();
            source.put(property.getName(), newValue);
            propertySources.addFirst(new AxilePropertySource(source));
        } else {
            var axilePropertySource = (AxilePropertySource) potentiallyAxilePropertySource;
            axilePropertySource.addProperty(property.getName(), newValue);
        }

        contextRestarter.restartContext();
    }
}
