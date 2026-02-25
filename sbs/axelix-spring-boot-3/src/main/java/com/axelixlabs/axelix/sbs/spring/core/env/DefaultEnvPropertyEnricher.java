/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.env;

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

import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Deprecation;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Property;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.PropertySource;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertySourceDescription.PropertySourceDisplayData;

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

    private final ValueInjectionTrackerBeanPostProcessor valueInjectionTracker;

    public DefaultEnvPropertyEnricher(
            Environment environment,
            PropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> cache,
            PropertyMetadataExtractor metadataExtractor,
            ValueInjectionTrackerBeanPostProcessor valueInjectionTracker) {
        this.configurationPropertiesCache = cache.getIfAvailable();
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.environment = environment;
        this.metadataExtractor = metadataExtractor;
        this.valueInjectionTracker = valueInjectionTracker;
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

                    List<InjectionPoint> injectionPoints =
                            valueInjectionTracker.getInjectionPointsForProperty(normalizedName);

                    return new Property(
                            propertyName,
                            stringValue,
                            isPrimary,
                            configPropsBeanName,
                            Optional.ofNullable(metadata)
                                    .map(PropertyMetadata::getDescription)
                                    .orElse(null),
                            buildFromMetadata(metadata),
                            injectionPoints);
                })
                .toList();

        PropertySourceDisplayData displayData = PropertySourceDescription.resolveDisplayData(source.getName());

        return new PropertySource(displayData.displayName(), displayData.description(), enrichedProperties);
    }

    @Nullable
    private Deprecation buildFromMetadata(@Nullable PropertyMetadata propertyMetadata) {
        if (propertyMetadata == null || propertyMetadata.getDeprecation() == null) {
            return null;
        }

        return new Deprecation(propertyMetadata.getDeprecation().getMessage());
    }

    private Map<String, String> buildConfigPropsMappingMap() {
        if (configurationPropertiesCache == null) {
            return Map.of();
        }

        Map<String, String> configPropsMapping = new HashMap<>();

        configurationPropertiesCache
                .getConfigProps()
                .getBeans()
                .forEach((bean) -> applyPrefixAndProperty(
                        bean.getPrefix(), bean.getProperties(), configPropsMapping, bean.getBeanName()));

        return configPropsMapping;
    }

    private void applyPrefixAndProperty(
            String prefix, List<KeyValue> properties, Map<String, String> configPropsMapping, String beanName) {
        for (var property : properties) {
            String fullProperty = propertyNameNormalizer.normalize(prefix + property.getKey());
            configPropsMapping.put(fullProperty, beanName);
        }
    }
}
