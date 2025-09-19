package com.nucleonforge.axile.master.service.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param auto Whether the discovery of managed services is supposed to happen automatically
 * by this Axile Master deployment.
 */
@ConfigurationProperties(prefix = "axile.master.discovery")
public record DiscoveryConfig(boolean auto) {}
