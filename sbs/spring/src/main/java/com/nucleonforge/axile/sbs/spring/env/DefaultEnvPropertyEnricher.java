package com.nucleonforge.axile.sbs.spring.env;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertyValueDescriptor;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ServiceConfigurationProperties;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint.AxileEnvironmentDescriptor;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint.AxilePropertySourceDescriptor;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint.AxilePropertyValueDescriptor;

/**
 * Default implementation {@link EnvPropertyEnricher}
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
public class DefaultEnvPropertyEnricher implements EnvPropertyEnricher {

    private final Environment environment;
    private final ServiceConfigurationProperties serviceConfigurationProperties;

    public DefaultEnvPropertyEnricher(
            Environment environment, ServiceConfigurationProperties serviceConfigurationProperties) {
        this.serviceConfigurationProperties = serviceConfigurationProperties;
        this.environment = environment;
    }

    @Override
    public AxileEnvironmentDescriptor enrich(EnvironmentDescriptor originalDescriptor) {
        Map<String, String> primarySourceMap = buildPrimarySourceMap(originalDescriptor);
        Map<String, String> properties = enrichPropertiesToBeanName();

        List<AxilePropertySourceDescriptor> enrichedSources = originalDescriptor.getPropertySources().stream()
                .map(source -> enrichPropertySource(source, primarySourceMap, properties))
                .toList();

        return new AxileEnvironmentDescriptor(
                originalDescriptor.getActiveProfiles(), List.of(environment.getDefaultProfiles()), enrichedSources);
    }

    private Map<String, String> buildPrimarySourceMap(EnvironmentDescriptor descriptor) {
        Map<String, String> primaryMap = new LinkedHashMap<>();

        // The built-in assumption here is that the property sources from the original spring endpoint
        // are returned in the order of their precedence, meaning, that the earlier property source
        // present in the list, the more priority it has over the other property sources. That is why
        // simple putIfAbsent is sufficient.
        for (PropertySourceDescriptor source : descriptor.getPropertySources()) {
            for (String key : source.getProperties().keySet()) {
                primaryMap.putIfAbsent(key, source.getName());
            }
        }
        return primaryMap;
    }

    private AxilePropertySourceDescriptor enrichPropertySource(
            PropertySourceDescriptor source, Map<String, String> primarySourceMap, Map<String, String> properties) {

        Map<String, AxilePropertyValueDescriptor> enrichedProperties = source.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PropertyValueDescriptor original = entry.getValue();
                    boolean isPrimary = Objects.equals(primarySourceMap.get(entry.getKey()), source.getName());

                    return new AxileEnvironmentEndpoint.AxilePropertyValueDescriptor(
                            original.getValue(),
                            original.getOrigin(),
                            isPrimary,
                            properties.getOrDefault(entry.getKey(), null));
                }));

        return new AxilePropertySourceDescriptor(source.getName(), enrichedProperties);
    }

    private Map<String, String> enrichPropertiesToBeanName() {
        Map<String, String> propertyToBeanName = new HashMap<>();

        serviceConfigurationProperties
                .getConfigurationProperties()
                .getContexts()
                .values()
                .forEach(context -> context.getBeans().forEach((beanName, bean) -> {
                    String cleanBeanName = stripBeanName(beanName);
                    flatten(bean.getPrefix(), bean.getProperties(), propertyToBeanName, cleanBeanName);
                }));

        return propertyToBeanName;
    }

    @SuppressWarnings("unchecked")
    private void flatten(
            String prefix, Map<String, Object> properties, Map<String, String> propertyToBeanName, String beanName) {
        properties.forEach((key, value) -> {
            String fullKey = prefix + "." + key;

            if (value instanceof Map<?, ?> map) {
                flatten(fullKey, (Map<String, Object>) map, propertyToBeanName, beanName);
            } else {
                propertyToBeanName.put(fullKey, beanName);
            }
        });
    }

    /**
     * The bean name of the configprops bean as returned by the actuator, for some reason, contains
     * the dash at the very beginning. I do not know why. We do not want to show it in the bean name.
     */
    private static String stripBeanName(String beanName) {
        int indexOfDash = beanName.indexOf("-");

        if (indexOfDash != -1 && indexOfDash < beanName.length() - 1) {
            return beanName.substring(indexOfDash + 1);
        } else {
            return beanName;
        }
    }
}
