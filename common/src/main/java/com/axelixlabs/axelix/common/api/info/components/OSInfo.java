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
package com.axelixlabs.axelix.common.api.info.components;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the OS information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class OSInfo {

    private final String name;
    private final String version;
    private final String arch;

    public OSInfo(
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("arch") String arch) {
        this.name = name;
        this.version = version;
        this.arch = arch;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String arch() {
        return arch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OSInfo osInfo = (OSInfo) o;
        return Objects.equals(name, osInfo.name)
                && Objects.equals(version, osInfo.version)
                && Objects.equals(arch, osInfo.arch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, arch);
    }

    @Override
    public String toString() {
        return "OSInfo{" + "name='" + name + '\'' + ", version='" + version + '\'' + ", arch='" + arch + '\'' + '}';
    }
}
