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
package com.axelixlabs.axelix.sbs.spring.env;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.sbs.spring.env.PropertyMetadata.Deprecation;

/**
 * Default implementation of {@link PropertyMetadataExtractor} that loads metadata from Spring Boot
 * configuration metadata files.
 *
 * <p>Automatically includes both {@code spring-configuration-metadata.json} (generated)
 * and {@code additional-spring-configuration-metadata.json} user-provided files,
 * which Spring Boot merges during compilation.</p>
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivkha
 */
public class DefaultPropertyMetadataExtractor implements PropertyMetadataExtractor {

    private static final String DEFAULT_DEPRECATION_MESSAGE =
            "Property marked as deprecated by spring-boot configuration processor";

    private static final String DEPRECATION_PREFIX = "Deprecated in favor of";

    private static final Logger log = LoggerFactory.getLogger(DefaultPropertyMetadataExtractor.class);

    private final ConfigurableEnvironment configurableEnvironment;
    private final PropertyNameNormalizer propertyNameNormalizer;
    private final Map<String, PropertyMetadata> metadataMap;
    private final ObjectMapper objectMapper;

    public DefaultPropertyMetadataExtractor(
            ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.metadataMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Nullable
    public PropertyMetadata getMetadata(String propertyName) {
        String normalizedName = propertyNameNormalizer.normalize(propertyName);
        return metadataMap.get(normalizedName);
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    void loadAndFilterPropertyMetadata() {
        loadPropertyMetadata();

        filterMetadata();
    }

    private void loadPropertyMetadata() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources("classpath*:**/spring-configuration-metadata.json");

            for (Resource resource : resources) {
                processMetadataResource(resource);
            }
        } catch (Exception ex) {
            log.warn(
                    "Unable to load spring-configuration-metadata.json files from the classpath. That would lead to absence of certain properties-related information",
                    ex);
        }
    }

    private void processMetadataResource(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(is);

            JsonNode propertiesNode = rootNode.get("properties");
            if (propertiesNode != null && propertiesNode.isArray()) {
                for (JsonNode propertyNode : propertiesNode) {
                    processPropertyNode(propertyNode);
                }
            }
        } catch (Exception ex) {
            log.warn(
                    "Unable to load resource from the classpath. Properties info specified in the resource will not be loaded",
                    ex);
        }
    }

    private void processPropertyNode(JsonNode propertyNode) {
        String name = extractTextOrNull(propertyNode, "name");
        if (name == null) {
            return;
        }

        String normalizedName = propertyNameNormalizer.normalize(name);
        if (metadataMap.containsKey(normalizedName)) {
            return;
        }

        PropertyMetadata propertyMetadata = buildPropertyMetadata(propertyNode);
        if (propertyMetadata != null) {
            metadataMap.put(normalizedName, propertyMetadata);
        }
    }

    @Nullable
    public PropertyMetadata buildPropertyMetadata(JsonNode propertyNode) {
        boolean deprecated = false;
        String reason = null;
        String replacement = null;

        String description = extractTextOrNull(propertyNode, "description");

        if (propertyNode.has("deprecated") && propertyNode.get("deprecated").isBoolean()) {
            deprecated = propertyNode.get("deprecated").asBoolean();
        }

        if (propertyNode.has("deprecation")) {
            JsonNode deprecationNode = propertyNode.get("deprecation");
            if (deprecationNode.isObject()) {
                deprecated = true;
                reason = extractTextOrNull(deprecationNode, "reason");
                replacement = extractTextOrNull(deprecationNode, "replacement");
            }
        }

        if (!deprecated && !StringUtils.hasText(description)) {
            return null;
        }

        if (deprecated) {
            String message = buildDeprecationMessage(reason, replacement);
            return new PropertyMetadata(description, new Deprecation(message));
        }

        return new PropertyMetadata(description, null);
    }

    // NullAway cannot infer that StringUtils.hasText(reason) guarantees a non-null value here.
    @SuppressWarnings("NullAway")
    private String buildDeprecationMessage(@Nullable String reason, @Nullable String replacement) {
        boolean hasReason = StringUtils.hasText(reason);
        boolean hasReplacement = StringUtils.hasText(replacement);

        if (hasReason && hasReplacement) {
            return "%s %s %s property.".formatted(reason, DEPRECATION_PREFIX, replacement);
        }

        if (hasReason) {
            return reason;
        }

        if (hasReplacement) {
            return "%s %s property.".formatted(DEPRECATION_PREFIX, replacement);
        }

        return DEFAULT_DEPRECATION_MESSAGE;
    }

    @Nullable
    private String extractTextOrNull(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }

    private void filterMetadata() {
        if (metadataMap.isEmpty()) {
            return;
        }

        Set<String> environmentPropertyNames = getAllPropertyNamesFromEnvironment();
        environmentPropertyNames = propertyNameNormalizer.normalizeAll(environmentPropertyNames, HashSet::new);

        Map<String, PropertyMetadata> filteredMap = new ConcurrentHashMap<>();

        for (String propertyName : environmentPropertyNames) {
            PropertyMetadata metadata = metadataMap.get(propertyName);
            if (metadata != null) {
                filteredMap.put(propertyName, metadata);
            }
        }

        metadataMap.clear();
        metadataMap.putAll(filteredMap);
    }

    private Set<String> getAllPropertyNamesFromEnvironment() {
        Set<String> propertyNames = new HashSet<>();

        for (PropertySource<?> source : configurableEnvironment.getPropertySources()) {
            propertyNames.addAll(extractPropertyNames(source));
        }

        return propertyNames;
    }

    private Set<String> extractPropertyNames(PropertySource<?> source) {
        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                extractPropertyNames(nest);
            }
        } else if (source instanceof EnumerablePropertySource<?> enumerable) {
            return Set.of(enumerable.getPropertyNames());
        }
        return Set.of();
    }
}
