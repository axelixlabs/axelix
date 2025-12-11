/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.PropertySourceDescriptor;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.Deprecation;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.Property;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;

/**
 * Default implementation {@link EnvPropertyEnricher}
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultEnvPropertyEnricher implements EnvPropertyEnricher {

    private final Environment environment;

    @Nullable
    private final ConfigurationPropertiesCache configurationPropertiesCache;

    private final PropertyNameNormalizer propertyNameNormalizer;

    private final PropertyMetadataExtractor metadataExtractor;

    public DefaultEnvPropertyEnricher(
            Environment environment,
            PropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache,
            PropertyMetadataExtractor metadataExtractor) {
        this.configurationPropertiesCache = cache.getIfAvailable();
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.environment = environment;
        this.metadataExtractor = metadataExtractor;
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

        List<Property> enrichedProperties = source.getProperties().entrySet().stream()
                .map(originalDescriptor -> {
                    String propertyName = originalDescriptor.getKey();
                    String normalizedName = propertyNameNormalizer.normalize(propertyName);
                    Object originalValue = originalDescriptor.getValue().getValue();
                    String stringValue = originalValue == null ? null : originalValue.toString();

                    boolean isPrimary = Objects.equals(primaryPropertySourceMap.get(normalizedName), source.getName());
                    String configPropsBeanName = configPropsMapping.getOrDefault(normalizedName, null);

                    PropertyMetadata metadata = metadataExtractor.getMetadata(normalizedName);

                    return new Property(
                            propertyName,
                            stringValue,
                            isPrimary,
                            configPropsBeanName,
                            Optional.ofNullable(metadata)
                                    .map(PropertyMetadata::description)
                                    .orElse(null),
                            buildFromMetadata(metadata));
                })
                .toList();

        return new PropertySource(
                source.getName(),
                PropertySourceDescription.getDescriptionBySourceName(source.getName()),
                enrichedProperties);
    }

    @Nullable
    private Deprecation buildFromMetadata(@Nullable PropertyMetadata propertyMetadata) {
        if (propertyMetadata == null || propertyMetadata.deprecation() == null) {
            return null;
        }

        return new Deprecation(
                propertyMetadata.deprecation().reason(),
                propertyMetadata.deprecation().replacement());
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
            String prefix, List<KeyValue> properties, Map<String, String> configPropsMapping, String beanName) {
        for (var property : properties) {
            String fullProperty = propertyNameNormalizer.normalize(prefix + property.key());
            configPropsMapping.put(fullProperty, beanName);
        }
    }
}
