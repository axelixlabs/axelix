package com.nucleonforge.axile.master.api.response.info.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given OS.
 *
 * @param name     The name of the operating system.
 * @param version  The version of the operating system.
 * @param arch     The architecture of the CPU.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OSProfile(String name, String version, String arch) {}
