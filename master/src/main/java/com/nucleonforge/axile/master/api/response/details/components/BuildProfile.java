package com.nucleonforge.axile.master.api.response.details.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given build.
 *
 * @param artifact     The artifact ID of the application.
 * @param version      The version of the application.
 * @param group        The group ID of the application.
 * @param time         The time the application was built.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BuildProfile(String artifact, String version, String group, String time) {}
