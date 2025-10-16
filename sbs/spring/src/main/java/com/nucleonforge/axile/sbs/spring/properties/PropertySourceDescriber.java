package com.nucleonforge.axile.sbs.spring.properties;

import org.springframework.core.env.PropertySource;

/**
 * Interface capable to assign a description to a given {@link PropertySource}.
 *
 * @author Mikhail Polivakha
 */
public interface PropertySourceDescriber {

    /**
     * Describe given {@link PropertySource}.
     *
     * @param propertySource property source to describe.
     * @return description of a given property source.
     */
    PropertySourceDescription describe(PropertySource<?> propertySource);
}
