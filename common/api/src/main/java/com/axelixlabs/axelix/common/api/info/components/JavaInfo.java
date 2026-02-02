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
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the java information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class JavaInfo {

    private final String version;

    @Nullable
    private final Vendor vendor;

    @Nullable
    private final Runtime runtime;

    @Nullable
    private final JVM jvm;

    public JavaInfo(
            @JsonProperty("version") String version,
            @JsonProperty("vendor") @Nullable Vendor vendor,
            @JsonProperty("runtime") @Nullable Runtime runtime,
            @JsonProperty("jvm") @Nullable JVM jvm) {
        this.version = version;
        this.vendor = vendor;
        this.runtime = runtime;
        this.jvm = jvm;
    }

    public String version() {
        return version;
    }

    @Nullable
    public Vendor vendor() {
        return vendor;
    }

    @Nullable
    public Runtime runtime() {
        return runtime;
    }

    @Nullable
    public JVM jvm() {
        return jvm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaInfo javaInfo = (JavaInfo) o;
        return Objects.equals(version, javaInfo.version)
                && Objects.equals(vendor, javaInfo.vendor)
                && Objects.equals(runtime, javaInfo.runtime)
                && Objects.equals(jvm, javaInfo.jvm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, vendor, runtime, jvm);
    }

    @Override
    public String toString() {
        return "JavaInfo{"
                + "version='"
                + version
                + '\''
                + ", vendor="
                + vendor
                + ", runtime="
                + runtime
                + ", jvm="
                + jvm
                + '}';
    }

    public static final class Vendor {

        private final String name;
        private final String version;

        public Vendor(@JsonProperty("name") String name, @JsonProperty("version") String version) {
            this.name = name;
            this.version = version;
        }

        public String name() {
            return name;
        }

        public String version() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Vendor vendor = (Vendor) o;
            return Objects.equals(name, vendor.name) && Objects.equals(version, vendor.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version);
        }

        @Override
        public String toString() {
            return "Vendor{" + "name='" + name + '\'' + ", version='" + version + '\'' + '}';
        }
    }

    public static final class Runtime {

        private final String name;
        private final String version;

        public Runtime(@JsonProperty("name") String name, @JsonProperty("version") String version) {
            this.name = name;
            this.version = version;
        }

        public String name() {
            return name;
        }

        public String version() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Runtime runtime = (Runtime) o;
            return Objects.equals(name, runtime.name) && Objects.equals(version, runtime.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version);
        }

        @Override
        public String toString() {
            return "Runtime{" + "name='" + name + '\'' + ", version='" + version + '\'' + '}';
        }
    }

    public static final class JVM {

        private final String name;
        private final String vendor;
        private final String version;

        public JVM(
                @JsonProperty("name") String name,
                @JsonProperty("vendor") String vendor,
                @JsonProperty("version") String version) {
            this.name = name;
            this.vendor = vendor;
            this.version = version;
        }

        public String name() {
            return name;
        }

        public String vendor() {
            return vendor;
        }

        public String version() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            JVM jvm = (JVM) o;
            return Objects.equals(name, jvm.name)
                    && Objects.equals(vendor, jvm.vendor)
                    && Objects.equals(version, jvm.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, vendor, version);
        }

        @Override
        public String toString() {
            return "JVM{" + "name='" + name + '\'' + ", vendor='" + vendor + '\'' + ", version='" + version + '\''
                    + '}';
        }
    }
}
