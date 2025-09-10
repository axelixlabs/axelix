package com.nucleonforge.axile.master.api.response.info.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given Java environment.
 *
 * @param version     The version of the Java.
 * @param vendor      The vendor details of the Java, if available.
 * @param runtime     The runtime details of the Java, if available.
 * @param jvm         The JVM details of the Java, if available.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JavaProfile(String version, @Nullable Vendor vendor, @Nullable Runtime runtime, @Nullable JVM jvm) {
    /**
     * The profile of a given vendor.
     *
     * @param name     The name of the vendor.
     * @param version  The version of the vendor.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Vendor(String name, String version) {}

    /**
     * The profile of a given runtime.
     *
     * @param name     The name of the runtime.
     * @param version  The version of the runtime.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Runtime(String name, String version) {}

    /**
     * The profile of a given JVM.
     *
     * @param name     The name of the JVM.
     * @param vendor   The vendor of the JVM
     * @param version  The version of the JVM.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JVM(String name, String vendor, String version) {}
}
