package com.nucleonforge.axile.sbs.spring.properties.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Default implementation of {@link InvalidPropertiesLoader}
 *
 * @since 28.11.2025
 * @author Nikita Kirillov
 */
public class YamlInvalidPropertiesLoader implements InvalidPropertiesLoader {

    private final String COMMON_INVALID_PROPERTIES_FILE = "classpath:common-invalid-properties.yml";
    private final String CUSTOM_INVALID_PROPERTIES_FILE = "classpath*:**/invalid-properties.*";
    private final PathMatchingResourcePatternResolver resolver;
    private final EnvironmentPropertyNameNormalizer normalizer;
    private final Yaml yaml;
    private final Map<String, List<InvalidPropertyValue>> invalidProperties;

    public YamlInvalidPropertiesLoader(EnvironmentPropertyNameNormalizer normalizer) {
        this.resolver = new PathMatchingResourcePatternResolver();
        this.normalizer = normalizer;
        this.yaml = new Yaml();
        this.invalidProperties = new HashMap<>();
        loadCommon();
        loadCustom();
    }

    @Override
    public Map<String, List<InvalidPropertyValue>> getInvalidProperties() {
        return invalidProperties;
    }

    @Override
    @Nullable
    public String getInvalidPropertyValues(String name, @Nullable Object value) {
        if (value == null) {
            return null;
        }

        String normalizedName = normalizer.normalize(name);

        List<InvalidPropertyValue> invalidValues = invalidProperties.get(normalizedName);

        if (invalidValues == null) {
            return null;
        }

        for (var invalidValue : invalidValues) {
            if (value.equals(String.valueOf(invalidValue.value()))) {
                return invalidValue.description();
            }
        }

        return null;
    }

    private void loadCommon() {
        try {
            Resource resource = resolver.getResource(COMMON_INVALID_PROPERTIES_FILE);
            loadIntoMap(resource);
        } catch (Exception ignored) {
        }
    }

    private void loadCustom() {
        try {
            Resource[] resources = resolver.getResources(CUSTOM_INVALID_PROPERTIES_FILE);
            for (Resource resource : resources) {
                loadIntoMap(resource);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"unchecked", "NullAway"})
    private void loadIntoMap(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            Map<String, Object> yamlContent = yaml.load(is);
            if (yamlContent == null || yamlContent.isEmpty()) {
                return;
            }

            for (Map.Entry<String, Object> entry : yamlContent.entrySet()) {

                String normalizedKey = normalizer.normalize(entry.getKey());
                Object value = entry.getValue();

                List<InvalidPropertyValue> values = new ArrayList<>();

                if (value instanceof List<?> list) {
                    for (Map<String, Object> item : (List<Map<String, Object>>) list) {
                        values.add(new InvalidPropertyValue(item.get("value"), (String) item.get("description")));
                    }
                }

                invalidProperties.put(normalizedKey, values);
            }
        }
    }
}
