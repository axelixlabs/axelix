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
public class BeansFeed {

    private final Map<String, Context> contexts;

    @JsonCreator
    public BeansFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }

    public static class Context {

        private final String parentId;
        private final Map<String, Bean> beans;

        @JsonCreator
        public Context(@JsonProperty("parentId") String parentId, @JsonProperty("beans") Map<String, Bean> beans) {
            this.parentId = parentId;
            this.beans = beans;
        }

        public String getParentId() {
            return parentId;
        }

        public Map<String, Bean> getBeans() {
            return beans;
        }
    }

    public static class Bean {

        private final String scope;
        private final String type;
        private final Set<String> aliases;
        private final Set<String> dependencies;

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

        public String getScope() {
            return scope;
        }

        public String getType() {
            return type;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }
    }
}
