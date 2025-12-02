package com.nucleonforge.axile.sbs.spring.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertyValueDescriptor;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertyValue;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.properties.utils.EnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.InvalidPropertiesLoader;

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

    private final EnvironmentPropertyNameNormalizer propertyNameNormalizer;

    private final InvalidPropertiesLoader invalidPropertiesLoader;

    public DefaultEnvPropertyEnricher(
            Environment environment,
            EnvironmentPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache,
            InvalidPropertiesLoader invalidPropertiesLoader) {
        this.configurationPropertiesCache = cache.getIfAvailable();
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.environment = environment;
        this.invalidPropertiesLoader = invalidPropertiesLoader;
    }

    @Override
    public EnvironmentFeed enrich(EnvironmentDescriptor originalDescriptor) {
        Map<String, String> primarySourceMap = buildPrimarySourceMap(originalDescriptor);
        Map<String, String> configPropsMapping = buildConfigPropsMappingMap();

        List<PropertySource> enrichedSources = originalDescriptor.getPropertySources().stream()
                .map(source -> enrichPropertySource(source, primarySourceMap, configPropsMapping))
                .toList();

        return new EnvironmentFeed(
                originalDescriptor.getActiveProfiles(),
                Arrays.stream(environment.getDefaultProfiles()).toList(),
                enrichedSources);
    }

    private Map<String, String> buildPrimarySourceMap(EnvironmentDescriptor descriptor) {
        Map<String, String> primaryMap = new LinkedHashMap<>();

        // The built-in assumption here is that the property sources from the original spring endpoint
        // are returned in the order of their precedence, meaning, that the earlier property source
        // present in the list, the more priority it has over the other property sources. That is why
        // simple putIfAbsent is sufficient.
        for (PropertySourceDescriptor source : descriptor.getPropertySources()) {
            for (String key : source.getProperties().keySet()) {
                primaryMap.putIfAbsent(propertyNameNormalizer.normalize(key), source.getName());
            }
        }
        return primaryMap;
    }

    private PropertySource enrichPropertySource(
            PropertySourceDescriptor source,
            Map<String, String> primaryPropertySourceMap,
            Map<String, String> configPropsMapping) {

        Map<String, PropertyValue> enrichedProperties = source.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PropertyValueDescriptor original = entry.getValue();

                    boolean isPrimary = Objects.equals(
                            primaryPropertySourceMap.get(propertyNameNormalizer.normalize(entry.getKey())),
                            source.getName());

                    String validationMessage = null;
                    Object value = original.getValue();
                    String stringValue = value == null ? null : value.toString();
                    if (isPrimary) {
                        validationMessage = invalidPropertiesLoader.getInvalidPropertyValues(entry.getKey(), value);
                    }

                    return new PropertyValue(
                            stringValue,
                            original.getOrigin(),
                            isPrimary,
                            configPropsMapping.getOrDefault(propertyNameNormalizer.normalize(entry.getKey()), null),
                            validationMessage);
                }));

        return new PropertySource(source.getName(), enrichedProperties);
    }

    private Map<String, String> buildConfigPropsMappingMap() {
        if (configurationPropertiesCache == null) {
            return Map.of();
        }

        Map<String, String> configPropsMapping = new HashMap<>();

        configurationPropertiesCache.getAxileConfigProps().contexts().values().forEach(context -> context.beans()
                .forEach((beanName, bean) -> {
                    applyPrefixAndProperty(bean.prefix(), bean.properties(), configPropsMapping, beanName);
                }));

        return configPropsMapping;
    }

    private void applyPrefixAndProperty(
            String prefix,
            List<ConfigPropsFeed.Property> properties,
            Map<String, String> configPropsMapping,
            String beanName) {
        for (var property : properties) {
            String fullProperty = propertyNameNormalizer.normalize(prefix + property.name());
            configPropsMapping.put(fullProperty, beanName);
        }
    }
}
