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
 * DTO that encapsulates the build information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class BuildInfo {

    private final String artifact;
    private final String name;
    private final String version;
    private final String group;
    private final String time;

    public BuildInfo(
            @JsonProperty("artifact") String artifact,
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("group") String group,
            @JsonProperty("time") String time) {
        this.artifact = artifact;
        this.name = name;
        this.version = version;
        this.group = group;
        this.time = time;
    }

    public String artifact() {
        return artifact;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String group() {
        return group;
    }

    public String time() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildInfo buildInfo = (BuildInfo) o;
        return Objects.equals(artifact, buildInfo.artifact)
                && Objects.equals(name, buildInfo.name)
                && Objects.equals(version, buildInfo.version)
                && Objects.equals(group, buildInfo.group)
                && Objects.equals(time, buildInfo.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifact, name, version, group, time);
    }

    @Override
    public String toString() {
        return "BuildInfo{"
                + "artifact='"
                + artifact
                + '\''
                + ", name='"
                + name
                + '\''
                + ", version='"
                + version
                + '\''
                + ", group='"
                + group
                + '\''
                + ", time='"
                + time
                + '\''
                + '}';
    }
}
