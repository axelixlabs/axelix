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

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * Configuration properties for self-registration of the service instance.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 * @author Cherkasov Sergey
 */
@SuppressWarnings("NullAway.Init")
public class SelfRegistrationConfigurationProperties implements Validatable {

    public static final String CONFIG_PROPS_PREFIX = "axelix.sbs.discovery";

    /**
     * The URL of the master that the service must connect to.
     */
    private String masterUrl;

    /**
     * The name of the service under which it will be registered in the master and subsequently displayed
     * in wallboard, mcp, etc.
     */
    private String instanceName;

    /**
     * The URL of the service, including the postfix for the actuator path, e.g. {@code https://my-app:6061/actuator}.
     * The master will use this URL to communicate with this service.
     */
    private String instanceActuatorUrl;

    /**
     * The interval of the frequency of self-registration of this service in the master.
     * By default, the interval is 15 seconds.
     */
    private Duration heartbeatInterval = Duration.of(15, ChronoUnit.SECONDS);

    public String getMasterUrl() {
        return masterUrl;
    }

    public String getInstanceActuatorUrl() {
        return instanceActuatorUrl;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setMasterUrl(String masterUrl) {
        validateUrl(masterUrl, "axelix.sbs.discovery.master-url");
        this.masterUrl = masterUrl;
    }

    public void setInstanceActuatorUrl(String instanceActuatorUrl) {
        validateUrl(instanceActuatorUrl, "axelix.sbs.discovery.instance-actuator-url");
        this.instanceActuatorUrl = cleanInstanceActuatorUrl(instanceActuatorUrl);
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setHeartbeatInterval(@Nullable Duration heartbeatInterval) {
        if (heartbeatInterval != null) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }

    @Override
    public void validate() {
        validateRequiredProperty(masterUrl, "axelix.sbs.discovery.master-url");
        validateRequiredProperty(instanceActuatorUrl, "axelix.sbs.discovery.instance-actuator-url");
        validateRequiredProperty(instanceName, "axelix.sbs.discovery.instance-name");
    }

    private void validateRequiredProperty(Object value, String propertyName) {
        Assert.notNull(
                value, String.format("Property '%s' must be set when self-registration is enabled", propertyName));
    }

    private void validateUrl(String url, String propertyName) {
        Assert.isTrue(
                isValidUrl(url), String.format("Property '%s' must be a valid URL, but was: %s", propertyName, url));
    }

    private String cleanInstanceActuatorUrl(String instanceActuatorUrl) {
        if (instanceActuatorUrl.endsWith("/")) {
            return instanceActuatorUrl.substring(0, instanceActuatorUrl.length() - 1);
        }
        return instanceActuatorUrl;
    }

    private boolean isValidUrl(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
