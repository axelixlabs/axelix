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
package com.axelixlabs.axelix.common.api.registration;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents extended service instance metadata supplied as part of the self-registration process.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class SelfRegistrationMetadata {

    private final BasicDiscoveryMetadata basicDiscoveryMetadata;
    private final String instanceId;
    private final String instanceName;
    private final String instanceActuatorUrl;
    private final String deploymentAt;

    /**
     * Creates a new {@link SelfRegistrationMetadata}.
     *
     * @param basicDiscoveryMetadata    the basic metadata of a service instance
     * @param instanceId                unique identifier (uid) of the service
     * @param instanceName              name of the service
     * @param instanceActuatorUrl       the URL of the service, including the postfix for the actuator path,
     *                                  e.g. {@code https://my-app:6061/actuator}.
     * @param deploymentAt              timestamp when the service was created
     */
    @JsonCreator
    public SelfRegistrationMetadata(
            @JsonProperty("basicDiscoveryMetadata") BasicDiscoveryMetadata basicDiscoveryMetadata,
            @JsonProperty("instanceId") String instanceId,
            @JsonProperty("instanceName") String instanceName,
            @JsonProperty("instanceActuatorUrl") String instanceActuatorUrl,
            @JsonProperty("deploymentAt") String deploymentAt) {
        this.basicDiscoveryMetadata = basicDiscoveryMetadata;
        this.instanceId = instanceId;
        this.instanceName = instanceName;
        this.instanceActuatorUrl = instanceActuatorUrl;
        this.deploymentAt = deploymentAt;
    }

    public BasicDiscoveryMetadata getBasicDiscoveryMetadata() {
        return basicDiscoveryMetadata;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getInstanceActuatorUrl() {
        return instanceActuatorUrl;
    }

    public String getDeploymentAt() {
        return deploymentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelfRegistrationMetadata that = (SelfRegistrationMetadata) o;
        return Objects.equals(basicDiscoveryMetadata, that.basicDiscoveryMetadata)
                && Objects.equals(instanceId, that.instanceId)
                && Objects.equals(instanceName, that.instanceName)
                && Objects.equals(instanceActuatorUrl, that.instanceActuatorUrl)
                && Objects.equals(deploymentAt, that.deploymentAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicDiscoveryMetadata, instanceId, instanceName, instanceActuatorUrl, deploymentAt);
    }

    @Override
    public String toString() {
        return "SelfRegistrationMetadata{"
                + "basicDiscoveryMetadata="
                + basicDiscoveryMetadata
                + ", instanceId='"
                + instanceId
                + '\''
                + ", instanceName='"
                + instanceName
                + '\''
                + ", instanceActuatorUrl='"
                + instanceActuatorUrl
                + '\''
                + ", deploymentAt='"
                + deploymentAt
                + '\''
                + '}';
    }
}
