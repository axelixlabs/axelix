package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the spring information of the given artifact.
 *
 * @param springBootVersion       The version of the Spring Boot.
 * @param springFrameworkVersion  The version of the Spring Framework.
 * @param springCloudVersion      The version of the Spring Cloud.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record SpringDetails(
        @JsonProperty("springBootVersion") String springBootVersion,
        @JsonProperty("springFrameworkVersion") String springFrameworkVersion,
        @JsonProperty("springCloudVersion") String springCloudVersion) {}
