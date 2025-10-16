package com.nucleonforge.axile.sbs.spring.properties;

import java.util.Map;

import org.springframework.core.env.MapPropertySource;

/**
 * A custom {@link MapPropertySource} implementation used to hold mutable property values
 * managed dynamically during application runtime.
 *
 * <p>This property source is registered under the name {@code AXILE_PROPERTY_SOURCE_NAME}
 * and is used to override or introduce configuration properties dynamically, through
 * actuator endpoints.
 *
 * @since 07.04.2025
 * @author Mikhail Polivakha
 */
public class AxilePropertySource extends MapPropertySource {

    public static final String AXILE_PROPERTY_SOURCE_NAME = "AXILE_PROPERTY_SOURCE_NAME";

    public AxilePropertySource(Map<String, Object> source) {
        super(AXILE_PROPERTY_SOURCE_NAME, source);
    }

    /**
     * Add new property value to property source
     *
     * @param name <strong>already resolved</strong> property name
     */
    public void addProperty(String name, String value) {
        super.source.put(name, value);
    }
}
