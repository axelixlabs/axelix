package com.nucleonforge.axile.common.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @author Mikhail Polivakha
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">Beans Endpoint</a>
 * @see ActuatorEndpoint
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

    public record Bean(String scope, String type, Set<String> aliases, Set<String> dependencies, boolean isPrimary,
                       boolean isLazyInit, List<String> qualifiers) {

            @JsonCreator
            public Bean(
                @JsonProperty("scope") String scope,
                @JsonProperty("type") String type,
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("dependencies") Set<String> dependencies,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("isLazyInit") boolean isLazyInit,
                @JsonProperty("qualifiers") List<String> qualifiers) {
                this.scope = scope;
                this.type = type;
                this.aliases = aliases;
                this.dependencies = dependencies;
                this.isPrimary = isPrimary;
                this.isLazyInit = isLazyInit;
                this.qualifiers = qualifiers;
            }
        }

    public enum BeanOrigin {
        COMPONENT_ANNOTATION,
        BEAN_METHOD,
        FACTORY_BEAN,
        UNKNOWN
    }
}
