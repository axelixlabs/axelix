package com.nucleonforge.axile.spring.master;

/**
 * The response to custom actuator endpoint - /actuator/axile-metadata.
 *
 * @param version the application version
 *
 * @since 18.09.2025
 * @author Nikita
 */
public record MetadataResponse(String version) {}
