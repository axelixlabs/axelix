package com.nucleonforge.axile.sbs.spring.env;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;

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
 */
public class DefaultPropertyMetadataExtractor implements PropertyMetadataExtractor {

    private final ConfigurableEnvironment configurableEnvironment;
    private final PropertyNameNormalizer propertyNameNormalizer;
    private final Map<String, PropertyMetadata> metadataMap;

    public DefaultPropertyMetadataExtractor(
            ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameNormalizer = propertyNameNormalizer;
        metadataMap = new ConcurrentHashMap<>();
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
        } catch (Exception ignored) {
        }
    }

    private void processMetadataResource(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(is);

            JsonNode propertiesNode = rootNode.get("properties");
            if (propertiesNode != null && propertiesNode.isArray()) {
                for (JsonNode propertyNode : propertiesNode) {
                    processPropertyNode(propertyNode);
                }
            }
        } catch (Exception ignored) {
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

        String description = extractTextOrNull(propertyNode, "description");

        boolean deprecated = false;
        String deprecatedReason = null;
        String deprecatedReplacement = null;

        if (propertyNode.has("deprecated") && propertyNode.get("deprecated").isBoolean()) {
            deprecated = propertyNode.get("deprecated").asBoolean();
        }

        if (propertyNode.has("deprecation")) {
            JsonNode deprecationNode = propertyNode.get("deprecation");
            if (deprecationNode.isObject()) {
                deprecated = true;
                deprecatedReason = extractTextOrNull(deprecationNode, "reason");
                deprecatedReplacement = extractTextOrNull(deprecationNode, "replacement");
            }
        }

        if (description != null || deprecated) {
            metadataMap.put(
                    normalizedName,
                    new PropertyMetadata(description, deprecated, deprecatedReason, deprecatedReplacement));
        }
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
