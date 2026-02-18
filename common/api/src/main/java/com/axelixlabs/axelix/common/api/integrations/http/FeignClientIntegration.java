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
package com.axelixlabs.axelix.common.api.integrations.http;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.axelixlabs.axelix.common.api.integrations.AbstractIntegration;

public class FeignClientIntegration extends AbstractIntegration {
    private final String entityType;
    private final String protocol;
    private final List<String> networkAddresses;
    private final List<HttpMethod> methodMappings;

    @JsonCreator
    public FeignClientIntegration(
            @JsonProperty("networkAddresses") List<String> networkAddresses,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("entityType") String entityType,
            @JsonProperty("methodMappings") List<HttpMethod> methodMappings) {
        super("", protocol, entityType);
        this.entityType = entityType;
        this.protocol = protocol;
        this.networkAddresses = networkAddresses;
        this.methodMappings = methodMappings;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<String> getNetworkAddresses() {
        return networkAddresses;
    }

    public List<HttpMethod> getMethodMappings() {
        return methodMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ;
        FeignClientIntegration that = (FeignClientIntegration) o;
        return Objects.equals(entityType, that.entityType)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(networkAddresses, that.networkAddresses)
                && Objects.equals(methodMappings, that.methodMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, protocol, networkAddresses, methodMappings);
    }

    @Override
    public String toString() {
        return "FeignClientIntegration{" + "entityType='"
                + entityType + '\'' + ", protocol='"
                + protocol + '\'' + ", networkAddresses="
                + networkAddresses + ", methodMappings="
                + methodMappings + '}';
    }

    public static class HttpMethod {
        private final String httpMethod;
        private final String path;

        @JsonCreator
        public HttpMethod(@JsonProperty("httpMethod") String httpMethod, @JsonProperty("path") String path) {
            this.httpMethod = httpMethod;
            this.path = path;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ;
            HttpMethod that = (HttpMethod) o;
            return Objects.equals(httpMethod, that.httpMethod) && Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(httpMethod, path);
        }

        @Override
        public String toString() {
            return "FeignClientMethodMapping{" + "httpMethod='" + httpMethod + '\'' + ", path='" + path + '\'' + '}';
        }
    }
}
