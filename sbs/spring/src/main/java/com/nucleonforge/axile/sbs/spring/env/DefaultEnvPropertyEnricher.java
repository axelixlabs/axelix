package com.nucleonforge.axile.sbs.spring.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertyValueDescriptor;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
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

    @Nullable
    private final ConfigurationPropertiesCache configurationPropertiesCache;

    public DefaultEnvPropertyEnricher(Environment environment, ObjectProvider<ConfigurationPropertiesCache> cache) {
        this.configurationPropertiesCache = cache.getIfAvailable();
        this.environment = environment;
    }

    @Override
    public AxileEnvironmentDescriptor enrich(EnvironmentDescriptor originalDescriptor) {
        Map<String, String> primarySourceMap = buildPrimarySourceMap(originalDescriptor);
        Map<ConfigurationPropertyName, String> configPropsMapping = buildConfigPropsMappingMap();

        List<AxilePropertySourceDescriptor> enrichedSources = originalDescriptor.getPropertySources().stream()
                .map(source -> enrichPropertySource(source, primarySourceMap, configPropsMapping))
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
            PropertySourceDescriptor source,
            Map<String, String> primaryPropertySourceMap,
            Map<ConfigurationPropertyName, String> configPropsMapping) {

        Map<String, AxilePropertyValueDescriptor> enrichedProperties = source.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PropertyValueDescriptor original = entry.getValue();

                    boolean isPrimary = Objects.equals(primaryPropertySourceMap.get(entry.getKey()), source.getName());

                    System.out.println(findBeanName(configPropsMapping, entry.getKey()));

                    return new AxileEnvironmentEndpoint.AxilePropertyValueDescriptor(
                            original.getValue(),
                            original.getOrigin(),
                            isPrimary,
                            findBeanName(configPropsMapping, entry.getKey()));
                }));

        return new AxilePropertySourceDescriptor(source.getName(), enrichedProperties);
    }

    private Map<ConfigurationPropertyName, String> buildConfigPropsMappingMap() {
        if (configurationPropertiesCache == null) {
            return Map.of();
        }

        Map<ConfigurationPropertyName, String> propertyToBeanName = new HashMap<>();

        configurationPropertiesCache.getConfigurationProperties()
            .getContexts()
            .values()
            .forEach(context -> context.getBeans().forEach((beanName, bean) -> {
                String cleanBeanName = stripConfigPropsPrefix(beanName);
                flatten(bean.getPrefix(), bean.getProperties(), propertyToBeanName, cleanBeanName);
            }));

        return propertyToBeanName;
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> properties,
                         Map<ConfigurationPropertyName, String> propertyToBeanName, String beanName) {
        properties.forEach((key, value) -> {
            String fullKey = prefix + "." + key;

            if (value instanceof Map<?, ?> map) {
                flatten(fullKey, (Map<String, Object>) map, propertyToBeanName, beanName);
            } else {
                Set<ConfigurationPropertyName> aliases = generateAliases(fullKey);
                for (ConfigurationPropertyName alias : aliases) {
                    propertyToBeanName.put(alias, beanName);
                }
            }
        });
    }

    public static String stripConfigPropsPrefix(String beanName) {
        int indexOfDash = beanName.indexOf("-");

        if (indexOfDash != -1 && indexOfDash < beanName.length() - 1) {
            return beanName.substring(indexOfDash + 1);
        } else {
            return beanName;
        }
    }

    private Set<ConfigurationPropertyName> generateAliases(String property) {
        Set<ConfigurationPropertyName> aliases = new HashSet<>();
        aliases.add(ConfigurationPropertyName.adapt(property, '.'));
        aliases.add(ConfigurationPropertyName.adapt(property, '_'));
        aliases.add(ConfigurationPropertyName.adapt(property, '-'));
        return aliases;
    }

    public static @Nullable String findBeanName(Map<ConfigurationPropertyName, String> map,
                                                String envProperty) {
        for (char sep : new char[]{'.', '_', '-'}) {
            ConfigurationPropertyName adapted = ConfigurationPropertyName.adapt(envProperty, sep);
            if (map.containsKey(adapted)) {
                return map.get(adapted);
            }
        }
        return null;
    }
}
