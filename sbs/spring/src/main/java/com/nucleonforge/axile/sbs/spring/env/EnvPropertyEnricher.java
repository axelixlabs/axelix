package com.nucleonforge.axile.sbs.spring.env;

import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;

import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint.AxileEnvironmentDescriptor;

/**
 * Enriches environment property information with additional metadata.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
public interface EnvPropertyEnricher {

    /**
     * Enriches the original environment descriptor with additional metadata.
     *
     * @param originalDescriptor the original environment descriptor from Spring Boot
     * @return enriched environment descriptor
     */
    AxileEnvironmentDescriptor enrich(EnvironmentDescriptor originalDescriptor);
}
