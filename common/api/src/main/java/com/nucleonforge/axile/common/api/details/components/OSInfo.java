package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the OS information of the given artifact.
 *
 * @param name     The name of the operating system.
 * @param version  The version of the operating system.
 * @param arch     The architecture of the CPU.
 *
 * @author Sergey Cherkasov
 */
public record OSInfo(
        @JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("arch") String arch) {}
