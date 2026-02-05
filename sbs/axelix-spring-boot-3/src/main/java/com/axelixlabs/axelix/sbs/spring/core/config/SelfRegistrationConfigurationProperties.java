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
package com.axelixlabs.axelix.sbs.spring.core.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Configuration properties for self-registration of the service instance.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 */
@ConfigurationProperties(prefix = "axelix.sbs.discovery")
public class SelfRegistrationConfigurationProperties {

    private final String masterUrl;

    private final String instanceUrl;

    private final String instanceName;

    private Duration heartbeatInterval = Duration.of(60, ChronoUnit.SECONDS);

    public SelfRegistrationConfigurationProperties(
            String masterUrl, String instanceUrl, String instanceName, @Nullable Duration heartbeatInterval) {
        validateRequiredProperty(masterUrl, "axelix.sbs.discovery.master-url");
        validateRequiredProperty(instanceUrl, "axelix.sbs.discovery.instance-url");
        validateRequiredProperty(instanceName, "axelix.sbs.discovery.instance-name");

        validateUrl(masterUrl, "axelix.sbs.discovery.master-url");
        validateUrl(instanceUrl, "axelix.sbs.discovery.instance-url");

        this.masterUrl = masterUrl;
        this.instanceUrl = instanceUrl;
        this.instanceName = instanceName;

        if (heartbeatInterval != null) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    private void validateRequiredProperty(Object value, String propertyName) {
        Assert.notNull(value, String.format("Property '%s' must be set when auto-discovery is enabled", propertyName));
    }

    private void validateUrl(String url, String propertyName) {
        Assert.isTrue(
                isValidUrl(url), String.format("Property '%s' must be a valid URL, but was: %s", propertyName, url));
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
