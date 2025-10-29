package com.nucleonforge.axile.master.api.response.details.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given spring.
 *
 * @param springBootVersion       The version of the Spring Boot.
 * @param springFrameworkVersion  The version of the Spring Framework.
 * @param springCloudVersion      The version of the Spring Cloud.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpringProfile(String springBootVersion, String springFrameworkVersion, String springCloudVersion) {}
