package com.nucleonforge.axile.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The response returned by the custom Axile details endpoint.
 *
 * @param instanceName  The name of the pod of the registered instance.
 * @param git           The DTO containing git component details.
 * @param spring        The DTO containing spring component details.
 * @param runtime       The DTO containing runtime component details.
 * @param build         The DTO containing build component details.
 * @param os            The DTO containing OS component details.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record AxileDetails(
        @Nullable String instanceName,
        @JsonProperty("git") @Nullable GitDetails git,
        @JsonProperty("spring") SpringDetails spring,
        @JsonProperty("runtime") RuntimeDetails runtime,
        @JsonProperty("build") @Nullable BuildDetails build,
        @JsonProperty("os") OsDetails os) {

    public AxileDetails setInstanceName(String instanceName) {
        return new AxileDetails(instanceName, this.git, this.spring, this.runtime, this.build, this.os);
    }

    /**
     * DTO that encapsulates the git information of the given artifact.
     *
     * @param commitShaShort     The ID of the commit.
     * @param branch             The name of the Git branch.
     * @param commitAuthor       The commit author information.
     * @param commitTimestamp    The timestamp of the commit.
     */
    public record GitDetails(
            @JsonProperty("commitShaShort") String commitShaShort,
            @JsonProperty("branch") String branch,
            @JsonProperty("commitAuthor") CommitAuthor commitAuthor,
            @JsonProperty("commitTimestamp") String commitTimestamp) {

        /**
         * Author of the commit information
         *
         * @param name         The commit author name.
         * @param email        The commit author email.
         */
        public record CommitAuthor(@JsonProperty("name") String name, @JsonProperty("email") String email) {}
    }

    /**
     * DTO that encapsulates the spring information of the given artifact.
     *
     * @param springBootVersion       The version of the Spring Boot.
     * @param springFrameworkVersion  The version of the Spring Framework.
     * @param springCloudVersion      The version of the Spring Cloud.
     */
    public record SpringDetails(
            @JsonProperty("springBootVersion") String springBootVersion,
            @JsonProperty("springFrameworkVersion") String springFrameworkVersion,
            @JsonProperty("springCloudVersion") String springCloudVersion) {}

    /**
     * DTO that encapsulates the Runtime information of the given artifact.
     *
     * @param javaVersion       The version of the java.
     * @param kotlinVersion     The version of the kotlin.
     * @param jdkVendor         The name of the vendor.
     * @param garbageCollector  The name of the garbage collector.
     */
    public record RuntimeDetails(
            @JsonProperty("javaVersion") String javaVersion,
            @JsonProperty("jdkVendor") String jdkVendor,
            @JsonProperty("garbageCollector") String garbageCollector,
            @JsonProperty("kotlinVersion") String kotlinVersion) {}

    /**
     * DTO that encapsulates the build information of the given artifact.
     *
     * @param artifact     The artifact ID of the application.
     * @param version      The version of the application.
     * @param group        The group ID of the application.
     * @param time         The time the application was built.
     */
    public record BuildDetails(
            @JsonProperty("artifact") String artifact,
            @JsonProperty("version") String version,
            @JsonProperty("group") String group,
            @JsonProperty("time") String time) {}

    /**
     * DTO that encapsulates the OS information of the given artifact.
     *
     * @param name     The name of the operating system.
     * @param version  The version of the operating system.
     * @param arch     The architecture of the CPU.
     */
    public record OsDetails(
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("arch") String arch) {}
}
