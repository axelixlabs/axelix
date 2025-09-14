package com.nucleonforge.axile.common.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to configprops actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html">Сonfigprops Endpoint</a>
 *
 * @author Sergey Cherkasov
 */
public record ConfigpropsFeed(@JsonProperty("contexts") Map<String, Context> contexts) {

    public record Context(@JsonProperty("beans") Map<String, Bean> beans, @JsonProperty("parentId") String parentId) {}

    public record Bean(
            @JsonProperty("prefix") String prefix,
            @JsonProperty("properties") Map<String, Object> properties,
            @JsonProperty("inputs") Map<String, Object> inputs) {}
}
