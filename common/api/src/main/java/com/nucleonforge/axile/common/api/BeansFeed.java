package com.nucleonforge.axile.common.api;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">Beans Endpoint</a>
 * @author Mikhail Polivakha
 */
public record BeansFeed(Map<String, Context> contexts) {

    @JsonCreator
    public BeansFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public record Context(String parentId, Map<String, Bean> beans) {

        @JsonCreator
        public Context(@JsonProperty("parentId") String parentId, @JsonProperty("beans") Map<String, Bean> beans) {
            this.parentId = parentId;
            this.beans = beans;
        }
    }

    public record Bean(String scope, String type, Set<String> aliases, Set<String> dependencies) {

        @JsonCreator
        public Bean(
                @JsonProperty("scope") String scope,
                @JsonProperty("type") String type,
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("dependencies") Set<String> dependencies) {
            this.scope = scope;
            this.type = type;
            this.aliases = aliases;
            this.dependencies = dependencies;
        }
    }
}
