package com.nucleonforge.axile.master.api.response.details.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given runtime.
 *
 * @param javaVersion       The version of the java.
 * @param kotlinVersion     The version of the kotlin.
 * @param jdkVendor         The name of the vendor.
 * @param garbageCollector  The name of the garbage collector.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RuntimeProfile(String javaVersion, String kotlinVersion, String jdkVendor, String garbageCollector) {}
