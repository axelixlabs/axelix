package com.nucleonforge.axile.master.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given details.
 *
 * @param serviceName  The name of the service providing information.
 * @param git          The profile of the git component response.
 * @param runtime      The profile of the runtime component response.
 * @param spring       The profile of the spring component response.
 * @param build        The profile of the build component response.
 * @param os           The profile of the OS component response.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AxileDetailsResponse(
        @Nullable String serviceName,
        @Nullable GitProfile git,
        RuntimeProfile runtime,
        SpringProfile spring,
        @Nullable BuildProfile build,
        OSProfile os) {

    /**
     * The profile of a given build.
     *
     * @param commitShaShort     The ID of the commit.
     * @param branch             The name of the Git branch.
     * @param authorName         The commit author name.
     * @param authorEmail        The commit author email.
     * @param commitTimestamp    The timestamp of the commit.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GitProfile(
            String commitShaShort, String branch, String authorName, String authorEmail, String commitTimestamp) {}

    /**
     * The profile of a given runtime.
     *
     * @param javaVersion       The version of the java.
     * @param kotlinVersion     The version of the kotlin.
     * @param jdkVendor         The name of the vendor.
     * @param garbageCollector  The name of the garbage collector.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RuntimeProfile(String javaVersion, String kotlinVersion, String jdkVendor, String garbageCollector) {}

    /**
     * The profile of a given spring.
     *
     * @param springBootVersion       The version of the Spring Boot.
     * @param springFrameworkVersion  The version of the Spring Framework.
     * @param springCloudVersion      The version of the Spring Cloud.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpringProfile(String springBootVersion, String springFrameworkVersion, String springCloudVersion) {}

    /**
     * The profile of a given build.
     *
     * @param artifact     The artifact ID of the application.
     * @param version      The version of the application.
     * @param group        The group ID of the application.
     * @param time         The time the application was built.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BuildProfile(String artifact, String version, String group, String time) {}

    /**
     * The profile of a given OS.
     *
     * @param name     The name of the operating system.
     * @param version  The version of the operating system.
     * @param arch     The architecture of the CPU.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OSProfile(String name, String version, String arch) {}
}
