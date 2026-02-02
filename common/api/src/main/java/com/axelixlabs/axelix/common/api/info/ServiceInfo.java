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
package com.axelixlabs.axelix.common.api.info;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.info.components.BuildInfo;
import com.axelixlabs.axelix.common.api.info.components.GitInfo;
import com.axelixlabs.axelix.common.api.info.components.JavaInfo;
import com.axelixlabs.axelix.common.api.info.components.OSInfo;
import com.axelixlabs.axelix.common.api.info.components.ProcessInfo;
import com.axelixlabs.axelix.common.api.info.components.SSLInfo;

/**
 * The response to info actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
public final class ServiceInfo {

    @Nullable
    private final GitInfo git;

    @Nullable
    private final BuildInfo build;

    @Nullable
    private final OSInfo os;

    @Nullable
    private final ProcessInfo process;

    @Nullable
    private final JavaInfo java;

    @Nullable
    private final SSLInfo ssl;

    public ServiceInfo(
            @JsonProperty("git") @Nullable GitInfo git,
            @JsonProperty("build") @Nullable BuildInfo build,
            @JsonProperty("os") @Nullable OSInfo os,
            @JsonProperty("process") @Nullable ProcessInfo process,
            @JsonProperty("java") @Nullable JavaInfo java,
            @JsonProperty("ssl") @Nullable SSLInfo ssl) {
        this.git = git;
        this.build = build;
        this.os = os;
        this.process = process;
        this.java = java;
        this.ssl = ssl;
    }

    @Nullable
    public GitInfo git() {
        return git;
    }

    @Nullable
    public BuildInfo build() {
        return build;
    }

    @Nullable
    public OSInfo os() {
        return os;
    }

    @Nullable
    public ProcessInfo process() {
        return process;
    }

    @Nullable
    public JavaInfo java() {
        return java;
    }

    @Nullable
    public SSLInfo ssl() {
        return ssl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInfo that = (ServiceInfo) o;
        return Objects.equals(git, that.git)
                && Objects.equals(build, that.build)
                && Objects.equals(os, that.os)
                && Objects.equals(process, that.process)
                && Objects.equals(java, that.java)
                && Objects.equals(ssl, that.ssl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(git, build, os, process, java, ssl);
    }

    @Override
    public String toString() {
        return "ServiceInfo{"
                + "git="
                + git
                + ", build="
                + build
                + ", os="
                + os
                + ", process="
                + process
                + ", java="
                + java
                + ", ssl="
                + ssl
                + '}';
    }
}
