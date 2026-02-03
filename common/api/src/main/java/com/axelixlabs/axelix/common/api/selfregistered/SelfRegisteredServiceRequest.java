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
package com.axelixlabs.axelix.common.api.selfregistered;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a request containing the required connection details of a self-registered service.
 *
 * @author Sergey Cherkasov
 */
public final class SelfRegisteredServiceRequest {
    String serviceId;
    String name;
    String address;
    int port;
    String deploymentAt;

    /**
     * Creates a new {@link SelfRegisteredServiceRequest}.
     *
     * @param serviceId     unique identifier (uid) of the service
     * @param name          name of the service
     * @param address       IP address
     * @param port          service port
     * @param deploymentAt  timestamp when the pod was created
     */
    @JsonCreator
    public SelfRegisteredServiceRequest(
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("port") int port,
            @JsonProperty("deploymentAt") String deploymentAt) {
        this.serviceId = serviceId;
        this.deploymentAt = deploymentAt;
        this.port = port;
        this.address = address;
        this.name = name;
    }

    public String serviceId() {
        return serviceId;
    }

    public String name() {
        return name;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    public String deploymentAt() {
        return deploymentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelfRegisteredServiceRequest that = (SelfRegisteredServiceRequest) o;
        return port == that.port
                && Objects.equals(name, that.name)
                && Objects.equals(address, that.address)
                && Objects.equals(deploymentAt, that.deploymentAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, port, deploymentAt);
    }

    @Override
    public String toString() {
        return "AxelixServiceRequest{" + "name='"
                + name + '\'' + ", address='"
                + address + '\'' + ", port="
                + port + ", deploymentAt='"
                + deploymentAt + '\'' + '}';
    }
}
