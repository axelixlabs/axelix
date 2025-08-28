package com.nucleonforge.axile.common.api;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">Beans Endpoint</a>
 * @author Mikhail Polivakha
 */
public class BeansFeed {

    private Map<String, Context> contexts;

    @JsonCreator
    public BeansFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    @JsonProperty("contexts")
    public Map<String, Context> getContexts() {
        return contexts;
    }

    @JsonProperty("contexts")
    public BeansFeed setContexts(Map<String, Context> contexts) {
        this.contexts = contexts;
        return this;
    }

    public static class Context {

        private String parentId;
        private Map<String, Bean> beans;

        @JsonCreator
        public Context(@JsonProperty("parentId") String parentId, @JsonProperty("beans") Map<String, Bean> beans) {
            this.parentId = parentId;
            this.beans = beans;
        }

        public String getParentId() {
            return parentId;
        }

        public Context setParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Map<String, Bean> getBeans() {
            return beans;
        }

        public Context setBeans(Map<String, Bean> beans) {
            this.beans = beans;
            return this;
        }
    }

    public static class Bean {

        private String scope;
        private String type;
        private Set<String> aliases;
        private Set<String> dependencies;

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

        public Set<String> getAliases() {
            return aliases;
        }

        @JsonSetter("aliases")
        public Bean setAliases(Set<String> aliases) {
            this.aliases = Objects.requireNonNullElseGet(aliases, HashSet::new);
            return this;
        }

        @JsonIgnore
        public Bean setAliases(String... aliases) {
            this.aliases = Set.of(aliases);
            return this;
        }

        public String getScope() {
            return scope;
        }

        public Bean setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public String getType() {
            return type;
        }

        public Bean setType(String type) {
            this.type = type;
            return this;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        @JsonSetter("dependencies")
        public Bean setDependencies(Set<String> dependencies) {
            this.dependencies = Objects.requireNonNullElseGet(dependencies, HashSet::new);
            return this;
        }

        @JsonIgnore
        public Bean setDependencies(String... dependencies) {
            this.dependencies = Set.of(dependencies);
            return this;
        }
    }
}
