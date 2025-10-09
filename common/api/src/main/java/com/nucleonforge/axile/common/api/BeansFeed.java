package com.nucleonforge.axile.common.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

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

    public record Bean(
            String scope,
            String type,
            Set<String> aliases,
            Set<String> dependencies,
            boolean isLazyInit,
            boolean isPrimary,
            List<String> qualifiers,
            @Nullable String enclosingClassName,
            @Nullable String methodName,
            @Nullable String factoryBeanName) {
        @JsonCreator
        public Bean(
                @JsonProperty("scope") String scope,
                @JsonProperty("type") String type,
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("dependencies") Set<String> dependencies,
                @JsonProperty("isLazyInit") boolean isLazyInit,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("qualifiers") List<String> qualifiers,
                @JsonProperty("enclosingClassName") @Nullable String enclosingClassName,
                @JsonProperty("methodName") @Nullable String methodName,
                @JsonProperty("factoryBeanName") @Nullable String factoryBeanName) {
            this.scope = scope;
            this.type = type;
            this.aliases = aliases != null ? aliases : Collections.emptySet();
            this.dependencies = dependencies != null ? dependencies : Collections.emptySet();
            this.isLazyInit = isLazyInit;
            this.isPrimary = isPrimary;
            this.qualifiers = qualifiers != null ? qualifiers : Collections.emptyList();
            this.enclosingClassName = enclosingClassName;
            this.methodName = methodName;
            this.factoryBeanName = factoryBeanName;
        }
    }
}
