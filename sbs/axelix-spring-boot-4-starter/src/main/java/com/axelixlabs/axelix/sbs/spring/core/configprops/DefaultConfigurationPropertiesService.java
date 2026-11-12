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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.Advised;
import org.springframework.boot.context.properties.BoundConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeedBean;
import com.axelixlabs.axelix.sbs.spring.core.utils.BeanNameUtils;

/**
 * Default implementation of {@link ConfigurationPropertiesService}.
 * Reads configuration properties beans and their binding inputs following Spring Boot 4.0.6's
 * {@code ConfigurationPropertiesReportEndpoint}, without Spring sanitization.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class DefaultConfigurationPropertiesService implements ConfigurationPropertiesService {

    private final ApplicationContext applicationContext;
    private final ConfigurationPropertiesFlattener flattener;
    private final PropertySensitivityMarker propertySensitivityMarker;
    private final ConfigurationPropertiesBeanSerializer serializer = getBeanSerializer();

    public DefaultConfigurationPropertiesService(
            ApplicationContext applicationContext,
            ConfigurationPropertiesFlattener flattener,
            PropertySensitivityMarker propertySensitivityMarker) {
        this.applicationContext = applicationContext;
        this.flattener = flattener;
        this.propertySensitivityMarker = propertySensitivityMarker;
    }

    @Override
    public ConfigurationPropertiesFeed getConfigProps() {
        List<ConfigurationPropertiesFeedBean> beans = new ArrayList<>();
        for (ApplicationContext context = applicationContext; context != null; context = context.getParent()) {
            for (ConfigurationPropertiesBean bean :
                    ConfigurationPropertiesBean.getAll(context).values()) {
                beans.add(describeBean(bean));
            }
        }
        return propertySensitivityMarker.mark(new ConfigurationPropertiesFeed().beans(beans));
    }

    private static ConfigurationPropertiesBeanSerializer getBeanSerializer() {
        ClassLoader classLoader = DefaultConfigurationPropertiesService.class.getClassLoader();
        if (ClassUtils.isPresent("tools.jackson.databind.json.JsonMapper", classLoader)) {
            return new JacksonConfigurationPropertiesBeanSerializer();
        }
        if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader)) {
            return new Jackson2ConfigurationPropertiesBeanSerializer();
        }
        return bean -> {
            throw new IllegalStateException("Jackson is required for the 'configprops' endpoint");
        };
    }

    @SuppressWarnings("NullAway")
    private ConfigurationPropertiesFeedBean describeBean(ConfigurationPropertiesBean bean) {
        String prefix = bean.getAnnotation().prefix();
        Map<String, @Nullable Object> serialized = safeSerialize(bean.getInstance(), prefix);
        Map<String, Object> inputs = getInputs(prefix, serialized);
        return new ConfigurationPropertiesFeedBean()
                .beanName(BeanNameUtils.stripConfigPropsPrefix(bean.getName()))
                .prefix(prefix)
                .properties(flattener.flatten(serialized))
                .inputs(flattener.flatten(inputs));
    }

    private Map<String, @Nullable Object> safeSerialize(@Nullable Object bean, String prefix) {
        try {
            if (bean instanceof Advised advised) {
                Object target = advised.getTargetSource().getTarget();
                try {
                    return safeSerialize(target, prefix);
                } finally {
                    if (target != null) {
                        advised.getTargetSource().releaseTarget(target);
                    }
                }
            }
            return new HashMap<>(serializer.serialize(bean));
        } catch (Exception ex) {
            return new HashMap<>(Map.of("error", "Cannot serialize '" + prefix + "'"));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputs(String prefix, Map<String, @Nullable Object> map) {
        Map<String, Object> augmented = new LinkedHashMap<>(map);
        map.forEach((key, value) -> {
            String qualifiedKey = getQualifiedKey(prefix, key);
            if (value instanceof Map) {
                augmented.put(key, getInputs(qualifiedKey, (Map<String, @Nullable Object>) value));
            } else if (value instanceof List) {
                augmented.put(key, getInputs(qualifiedKey, (List<Object>) value));
            } else {
                augmented.put(key, applyInput(qualifiedKey));
            }
        });
        return augmented;
    }

    @SuppressWarnings("unchecked")
    private List<Object> getInputs(String prefix, List<Object> list) {
        List<Object> augmented = new ArrayList<>();
        int index = 0;
        for (Object item : list) {
            String name = prefix + "[" + index++ + "]";
            if (item instanceof Map) {
                augmented.add(getInputs(name, (Map<String, @Nullable Object>) item));
            } else if (item instanceof List) {
                augmented.add(getInputs(name, (List<Object>) item));
            } else {
                augmented.add(applyInput(name));
            }
        }
        return augmented;
    }

    private String getQualifiedKey(String prefix, String key) {
        return (prefix.isEmpty() ? prefix : prefix + ".") + key;
    }

    private Map<String, @Nullable Object> applyInput(String qualifiedKey) {
        ConfigurationPropertyName currentName = getCurrentName(qualifiedKey);
        ConfigurationProperty candidate = getCandidate(currentName);
        PropertySource<?> propertySource = getPropertySource(candidate);
        if (propertySource != null) {
            Assert.state(candidate != null, "'candidate' must not be null");
            Object value = stringifyIfNecessary(candidate.getValue());
            return getInput(candidate, value);
        }
        return Collections.emptyMap();
    }

    private ConfigurationPropertyName getCurrentName(String qualifiedKey) {
        return ConfigurationPropertyName.adapt(qualifiedKey, '.');
    }

    private @Nullable ConfigurationProperty getCandidate(ConfigurationPropertyName currentName) {
        BoundConfigurationProperties bound = BoundConfigurationProperties.get(applicationContext);
        if (bound == null) {
            return null;
        }
        ConfigurationProperty candidate = bound.get(currentName);
        if (candidate == null && currentName.isLastElementIndexed()) {
            candidate = bound.get(currentName.chop(currentName.getNumberOfElements() - 1));
        }
        return candidate;
    }

    private @Nullable PropertySource<?> getPropertySource(@Nullable ConfigurationProperty configurationProperty) {
        if (configurationProperty == null) {
            return null;
        }
        ConfigurationPropertySource source = configurationProperty.getSource();
        Object underlyingSource = (source != null) ? source.getUnderlyingSource() : null;
        return (underlyingSource instanceof PropertySource<?>) ? (PropertySource<?>) underlyingSource : null;
    }

    private @Nullable Object stringifyIfNecessary(@Nullable Object value) {
        if (value == null || ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
            return value;
        }
        if (value instanceof CharSequence) {
            return value.toString();
        }
        return "Complex property value " + value.getClass().getName();
    }

    private Map<String, @Nullable Object> getInput(ConfigurationProperty candidate, @Nullable Object value) {
        Map<String, @Nullable Object> input = new LinkedHashMap<>();
        Origin origin = Origin.from(candidate);
        List<Origin> originParents = Origin.parentsFrom(candidate);
        input.put("value", value);
        input.put("origin", (origin != null) ? origin.toString() : "none");
        if (!originParents.isEmpty()) {
            input.put(
                    "originParents",
                    originParents.stream().map(Object::toString).toArray(String[]::new));
        }
        return input;
    }
}
