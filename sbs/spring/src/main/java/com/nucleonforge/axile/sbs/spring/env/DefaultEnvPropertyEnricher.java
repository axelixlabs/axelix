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

import com.nucleonforge.axile.common.utils.BeanNameUtils;
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

    private final EnvironmentPropertyNameNormalizer propertyNameNormalizer;

    public DefaultEnvPropertyEnricher(
            Environment environment,
            EnvironmentPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache) {
        this.configurationPropertiesCache = cache.getIfAvailable();
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.environment = environment;
    }

    @Override
    public AxileEnvironmentDescriptor enrich(EnvironmentDescriptor originalDescriptor) {
        Map<String, String> primarySourceMap = buildPrimarySourceMap(originalDescriptor);
        Map<String, String> configPropsMapping = buildConfigPropsMappingMap();

        List<AxilePropertySourceDescriptor> enrichedSources = originalDescriptor.getPropertySources().stream()
                .map(source -> enrichPropertySource(source, primarySourceMap, configPropsMapping))
                .toList();

        return new AxileEnvironmentDescriptor(
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
                primaryMap.putIfAbsent(key, source.getName());
            }
        }
        return primaryMap;
    }

    private AxilePropertySourceDescriptor enrichPropertySource(
            PropertySourceDescriptor source,
            Map<String, String> primaryPropertySourceMap,
            Map<String, String> configPropsMapping) {

        Map<String, AxilePropertyValueDescriptor> enrichedProperties = source.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PropertyValueDescriptor original = entry.getValue();

                    boolean isPrimary = Objects.equals(primaryPropertySourceMap.get(entry.getKey()), source.getName());

                    return new AxileEnvironmentEndpoint.AxilePropertyValueDescriptor(
                            original.getValue(),
                            original.getOrigin(),
                            isPrimary,
                            configPropsMapping.getOrDefault(propertyNameNormalizer.normalize(entry.getKey()), null));
                }));

        return new AxilePropertySourceDescriptor(source.getName(), enrichedProperties);
    }

    private Map<String, String> buildConfigPropsMappingMap() {
        if (configurationPropertiesCache == null) {
            return Map.of();
        }

        Map<String, String> propertyToBeanName = new HashMap<>();

        configurationPropertiesCache
                .getConfigurationProperties()
                .getContexts()
                .values()
                .forEach(context -> context.getBeans().forEach((beanName, bean) -> {
                    String cleanBeanName = BeanNameUtils.stripConfigPropsPrefix(beanName);
                    flatten(bean.getPrefix(), bean.getProperties(), propertyToBeanName, cleanBeanName);
                }));

        return propertyToBeanName;
    }

    @SuppressWarnings("unchecked")
    private void flatten(
            String prefix, Map<String, Object> properties, Map<String, String> propertyToBeanName, String beanName) {
        properties.forEach((key, value) -> {
            String propertiesName = prefix + "." + key;

            if (value instanceof Map<?, ?> map) {
                flatten(propertiesName, (Map<String, Object>) map, propertyToBeanName, beanName);
            } else {
                propertyToBeanName.put(propertyNameNormalizer.normalize(propertiesName), beanName);
            }
        });
    }
}
