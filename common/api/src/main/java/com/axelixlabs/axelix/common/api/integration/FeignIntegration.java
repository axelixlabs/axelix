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
package com.axelixlabs.axelix.common.api.integration;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that describes a discovered Feign integration exposed by Axelix endpoints.
 *
 * @author Sergey Cherkasov
 */
public class FeignIntegration {

    private final String serviceName;
    private final List<String> networkAddresses;
    private final String protocol;
    private final List<FeignHttpMethod> httpMethods;

    /**
     * Creates a new FeignIntegration.
     *
     * @param serviceName       service name from {@code @FeignClient}.
     * @param networkAddresses  may contain one or more URLs (for example, when obtained from a Discovery Client with
     *                          multiple registered service replicas). The value may also be empty if the FeignClient
     *                          cannot resolve the URL or if it was intentionally not provided in the @FeignClient
     *                          annotation. URLs follow the format{@code protocol://host:port}
     *                          (for example, {@code http://localhost:8080}).
     * @param protocol          HTTP protocol display name (for example, {@code HTTP/1.1}).
     * @param httpMethods       methods declared on the Feign client interface.
     */
    @JsonCreator
    public FeignIntegration(
            @JsonProperty("serviceName") String serviceName,
            @JsonProperty("networkAddresses") List<String> networkAddresses,
            @JsonProperty("protocol") String protocol,
            @JsonProperty("httpMethods") List<FeignHttpMethod> httpMethods) {
        this.serviceName = serviceName;
        this.networkAddresses = networkAddresses;
        this.protocol = protocol;
        this.httpMethods = httpMethods;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<String> getNetworkAddresses() {
        return networkAddresses;
    }

    public List<FeignHttpMethod> getHttpMethods() {
        return httpMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FeignIntegration that = (FeignIntegration) o;
        return Objects.equals(serviceName, that.serviceName)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(networkAddresses, that.networkAddresses)
                && Objects.equals(httpMethods, that.httpMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, protocol, networkAddresses, httpMethods);
    }

    @Override
    public String toString() {
        return "FeignClientIntegration{" + "serviceName='"
                + serviceName + '\'' + ", protocol='"
                + protocol + '\'' + ", networkAddresses="
                + networkAddresses + ", httpMethods="
                + httpMethods + '}';
    }

    /**
     * DTO that describes a single HTTP method declared on a Feign client.
     */
    public static class FeignHttpMethod {
        private final String httpMethod;

        @Nullable
        private final String path;

        /**
         * Creates a new FeignHttpMethod.
         *
         * @param httpMethod    HTTP method name (for example, {@code GET}, {@code POST}),
         *                      or {@code UNKNOWN} if the method is not specified.
         * @param path          mapping path associated with the method, or {@code null} if none is defined.
         */
        @JsonCreator
        public FeignHttpMethod(
                @JsonProperty("httpMethod") String httpMethod, @JsonProperty("path") @Nullable String path) {
            this.httpMethod = httpMethod;
            this.path = path;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public @Nullable String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FeignHttpMethod that = (FeignHttpMethod) o;
            return Objects.equals(httpMethod, that.httpMethod) && Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(httpMethod, path);
        }

        @Override
        public String toString() {
            return "FeignHttpMethod{" + "httpMethod='" + httpMethod + '\'' + ", path='" + path + '\'' + '}';
        }
    }
}
