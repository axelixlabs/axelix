package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

/**
 * Description of agiven {@link org.springframework.core.env.PropertySource}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public record PropertySourceDescription(
        String name, PropertySourceOrigin origin, Class<?> clazz, @Nullable String fileName) {

    enum PropertySourceOrigin {
        PROPERTIES_FILE,
        ENVIRONMENT_VARIABLES,
        SYSTEM_ARGS,
        CUSTOM,
    }
}
