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
package com.axelixlabs.axelix.sbs.spring.core.integrations;

import java.util.List;
import java.util.Objects;

/**
 * Holds resolved Feign integration endpoint data and is used as a transport container.
 *
 * @author Sergey Cherkasov
 */
public class FeignPathHolder {
    private final List<String> networkAddresses;
    private final String path;

    /**
     * Creates a new FeignPathHolder.
     *
     * @param networkAddresses may contain one or more URLs (for example, when obtained from a Discovery Client with
     *                         multiple registered service replicas). The value may also be empty if the FeignClient
     *                         cannot resolve the URL or if it was intentionally not provided in the @FeignClient
     *                         annotation. URLs follow the format{@code protocol://host:port}
     *                         (for example, {@code http://localhost:8080}).
     * @param path             the path defined in the @FeignClient annotation.
     */
    public FeignPathHolder(List<String> networkAddresses, String path) {
        this.networkAddresses = networkAddresses;
        this.path = path;
    }

    public List<String> getNetworkAddresses() {
        return networkAddresses;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeignPathHolder that = (FeignPathHolder) o;
        return Objects.equals(networkAddresses, that.networkAddresses) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkAddresses, path);
    }

    @Override
    public String toString() {
        return "PathTransport{" + "networkAddresses=" + networkAddresses + ", path='" + path + '\'' + '}';
    }
}
