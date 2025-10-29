package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the Runtime information of the given artifact.
 *
 * @param javaVersion       The version of the java.
 * @param kotlinVersion     The version of the kotlin.
 * @param jdkVendor         The name of the vendor.
 * @param garbageCollector  The name of the garbage collector.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record RuntimeDetails(
        @JsonProperty("javaVersion") String javaVersion,
        @JsonProperty("jdkVendor") String jdkVendor,
        @JsonProperty("garbageCollector") String garbageCollector,
        @JsonProperty("kotlinVersion") String kotlinVersion) {}
