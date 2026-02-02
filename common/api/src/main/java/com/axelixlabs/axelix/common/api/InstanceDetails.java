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
package com.axelixlabs.axelix.common.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The response returned by the custom details endpoint.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public final class InstanceDetails {

    private final GitDetails git;
    private final SpringDetails spring;
    private final RuntimeDetails runtime;
    private final BuildDetails build;
    private final OsDetails os;

    /**
     * Creates a new InstanceDetails.
     *
     * @param git     The DTO containing git component details.
     * @param spring  The DTO containing spring component details.
     * @param runtime The DTO containing runtime component details.
     * @param build   The DTO containing build component details.
     * @param os      The DTO containing OS component details.
     */
    @JsonCreator
    public InstanceDetails(
            @JsonProperty("git") GitDetails git,
            @JsonProperty("spring") SpringDetails spring,
            @JsonProperty("runtime") RuntimeDetails runtime,
            @JsonProperty("build") BuildDetails build,
            @JsonProperty("os") OsDetails os) {
        this.git = git;
        this.spring = spring;
        this.runtime = runtime;
        this.build = build;
        this.os = os;
    }

    public GitDetails getGit() {
        return git;
    }

    public SpringDetails getSpring() {
        return spring;
    }

    public RuntimeDetails getRuntime() {
        return runtime;
    }

    public BuildDetails getBuild() {
        return build;
    }

    public OsDetails getOs() {
        return os;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstanceDetails that = (InstanceDetails) o;
        return Objects.equals(git, that.git)
                && Objects.equals(spring, that.spring)
                && Objects.equals(runtime, that.runtime)
                && Objects.equals(build, that.build)
                && Objects.equals(os, that.os);
    }

    @Override
    public int hashCode() {
        return Objects.hash(git, spring, runtime, build, os);
    }

    @Override
    public String toString() {
        return "InstanceDetails{"
                + "git="
                + git
                + ", spring="
                + spring
                + ", runtime="
                + runtime
                + ", build="
                + build
                + ", os="
                + os
                + '}';
    }

    /**
     * DTO that encapsulates the git information of the given artifact.
     */
    public static final class GitDetails {

        private final String commitShaShort;
        private final String branch;
        private final CommitAuthor commitAuthor;
        private final String commitTimestamp;

        /**
         * Creates a new GitDetails.
         *
         * @param commitShaShort  The ID of the commit.
         * @param branch          The name of the Git branch.
         * @param commitAuthor    The commit author information.
         * @param commitTimestamp The timestamp of the commit.
         */
        @JsonCreator
        public GitDetails(
                @JsonProperty("commitShaShort") String commitShaShort,
                @JsonProperty("branch") String branch,
                @JsonProperty("commitAuthor") CommitAuthor commitAuthor,
                @JsonProperty("commitTimestamp") String commitTimestamp) {
            this.commitShaShort = commitShaShort;
            this.branch = branch;
            this.commitAuthor = commitAuthor;
            this.commitTimestamp = commitTimestamp;
        }

        public String getCommitShaShort() {
            return commitShaShort;
        }

        public String getBranch() {
            return branch;
        }

        public CommitAuthor getCommitAuthor() {
            return commitAuthor;
        }

        public String getCommitTimestamp() {
            return commitTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            GitDetails that = (GitDetails) o;
            return Objects.equals(commitShaShort, that.commitShaShort)
                    && Objects.equals(branch, that.branch)
                    && Objects.equals(commitAuthor, that.commitAuthor)
                    && Objects.equals(commitTimestamp, that.commitTimestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(commitShaShort, branch, commitAuthor, commitTimestamp);
        }

        @Override
        public String toString() {
            return "GitDetails{"
                    + "commitShaShort='"
                    + commitShaShort
                    + '\''
                    + ", branch='"
                    + branch
                    + '\''
                    + ", commitAuthor="
                    + commitAuthor
                    + ", commitTimestamp='"
                    + commitTimestamp
                    + '\''
                    + '}';
        }

        /**
         * Author of the commit information
         */
        public static final class CommitAuthor {

            private final String name;
            private final String email;

            /**
             * Creates a new CommitAuthor.
             *
             * @param name  The commit author name.
             * @param email The commit author email.
             */
            @JsonCreator
            public CommitAuthor(@JsonProperty("name") String name, @JsonProperty("email") String email) {
                this.name = name;
                this.email = email;
            }

            public String getName() {
                return name;
            }

            public String getEmail() {
                return email;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                CommitAuthor that = (CommitAuthor) o;
                return Objects.equals(name, that.name) && Objects.equals(email, that.email);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, email);
            }

            @Override
            public String toString() {
                return "CommitAuthor{" + "name='" + name + '\'' + ", email='" + email + '\'' + '}';
            }
        }
    }

    /**
     * DTO that encapsulates the spring information of the given artifact.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class SpringDetails {

        private final String springBootVersion;
        private final String springFrameworkVersion;

        @Nullable
        private final String springCloudVersion;

        /**
         * Creates a new SpringDetails.
         *
         * @param springBootVersion      The version of the Spring Boot.
         * @param springFrameworkVersion The version of the Spring Framework.
         * @param springCloudVersion     The version of the Spring Cloud, if any.
         */
        @JsonCreator
        public SpringDetails(
                @JsonProperty("springBootVersion") String springBootVersion,
                @JsonProperty("springFrameworkVersion") String springFrameworkVersion,
                @JsonProperty("springCloudVersion") @Nullable String springCloudVersion) {
            this.springBootVersion = springBootVersion;
            this.springFrameworkVersion = springFrameworkVersion;
            this.springCloudVersion = springCloudVersion;
        }

        public String getSpringBootVersion() {
            return springBootVersion;
        }

        public String getSpringFrameworkVersion() {
            return springFrameworkVersion;
        }

        @Nullable
        public String getSpringCloudVersion() {
            return springCloudVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SpringDetails that = (SpringDetails) o;
            return Objects.equals(springBootVersion, that.springBootVersion)
                    && Objects.equals(springFrameworkVersion, that.springFrameworkVersion)
                    && Objects.equals(springCloudVersion, that.springCloudVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(springBootVersion, springFrameworkVersion, springCloudVersion);
        }

        @Override
        public String toString() {
            return "SpringDetails{"
                    + "springBootVersion='"
                    + springBootVersion
                    + '\''
                    + ", springFrameworkVersion='"
                    + springFrameworkVersion
                    + '\''
                    + ", springCloudVersion='"
                    + springCloudVersion
                    + '\''
                    + '}';
        }
    }

    /**
     * DTO that encapsulates the Runtime information of the given artifact.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class RuntimeDetails {

        private final String javaVersion;
        private final String jdkVendor;
        private final String garbageCollector;

        @Nullable
        private final String kotlinVersion;

        /**
         * Creates a new RuntimeDetails.
         *
         * @param javaVersion      The version of the java.
         * @param jdkVendor        The name of the vendor.
         * @param garbageCollector The name of the garbage collector.
         * @param kotlinVersion    The version of the kotlin, if any.
         */
        @JsonCreator
        public RuntimeDetails(
                @JsonProperty("javaVersion") String javaVersion,
                @JsonProperty("jdkVendor") String jdkVendor,
                @JsonProperty("garbageCollector") String garbageCollector,
                @JsonProperty("kotlinVersion") @Nullable String kotlinVersion) {
            this.javaVersion = javaVersion;
            this.jdkVendor = jdkVendor;
            this.garbageCollector = garbageCollector;
            this.kotlinVersion = kotlinVersion;
        }

        public String getJavaVersion() {
            return javaVersion;
        }

        public String getJdkVendor() {
            return jdkVendor;
        }

        public String getGarbageCollector() {
            return garbageCollector;
        }

        @Nullable
        public String getKotlinVersion() {
            return kotlinVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RuntimeDetails that = (RuntimeDetails) o;
            return Objects.equals(javaVersion, that.javaVersion)
                    && Objects.equals(jdkVendor, that.jdkVendor)
                    && Objects.equals(garbageCollector, that.garbageCollector)
                    && Objects.equals(kotlinVersion, that.kotlinVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(javaVersion, jdkVendor, garbageCollector, kotlinVersion);
        }

        @Override
        public String toString() {
            return "RuntimeDetails{"
                    + "javaVersion='"
                    + javaVersion
                    + '\''
                    + ", jdkVendor='"
                    + jdkVendor
                    + '\''
                    + ", garbageCollector='"
                    + garbageCollector
                    + '\''
                    + ", kotlinVersion='"
                    + kotlinVersion
                    + '\''
                    + '}';
        }
    }

    /**
     * DTO that encapsulates the build information of the given artifact.
     */
    public static final class BuildDetails {

        private final String artifact;
        private final String version;
        private final String group;
        private final String time;

        /**
         * Creates a new BuildDetails.
         *
         * @param artifact The artifact ID of the application.
         * @param version  The version of the application.
         * @param group    The group ID of the application.
         * @param time     The time the application was built.
         */
        public BuildDetails(
                @JsonProperty("artifact") String artifact,
                @JsonProperty("version") String version,
                @JsonProperty("group") String group,
                @JsonProperty("time") String time) {
            this.artifact = artifact;
            this.version = version;
            this.group = group;
            this.time = time;
        }

        public String getArtifact() {
            return artifact;
        }

        public String getVersion() {
            return version;
        }

        public String getGroup() {
            return group;
        }

        public String getTime() {
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
            BuildDetails that = (BuildDetails) o;
            return Objects.equals(artifact, that.artifact)
                    && Objects.equals(version, that.version)
                    && Objects.equals(group, that.group)
                    && Objects.equals(time, that.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(artifact, version, group, time);
        }

        @Override
        public String toString() {
            return "BuildDetails{"
                    + "artifact='"
                    + artifact
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

    /**
     * DTO that encapsulates the OS information of the given artifact.
     */
    public static final class OsDetails {

        private final String name;
        private final String version;
        private final String arch;

        /**
         * Creates a new OsDetails.
         *
         * @param name    The name of the operating system.
         * @param version The version of the operating system.
         * @param arch    The architecture of the CPU.
         */
        public OsDetails(
                @JsonProperty("name") String name,
                @JsonProperty("version") String version,
                @JsonProperty("arch") String arch) {
            this.name = name;
            this.version = version;
            this.arch = arch;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getArch() {
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
            OsDetails osDetails = (OsDetails) o;
            return Objects.equals(name, osDetails.name)
                    && Objects.equals(version, osDetails.version)
                    && Objects.equals(arch, osDetails.arch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version, arch);
        }

        @Override
        public String toString() {
            return "OsDetails{"
                    + "name='"
                    + name
                    + '\''
                    + ", version='"
                    + version
                    + '\''
                    + ", arch='"
                    + arch
                    + '\''
                    + '}';
        }
    }
}
