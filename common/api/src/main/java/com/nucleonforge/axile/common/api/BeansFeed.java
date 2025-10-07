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
        private final boolean isPrimary;
        private final boolean isLazyInit;
        private final List<String> qualifiers;

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

        public boolean isPrimary() {
            return isPrimary;
        }

        public boolean isLazyInit() {
            return isLazyInit;
        }

        public List<String> getQualifiers() {
            return qualifiers;
        }
    }

    public enum BeanOrigin {
        COMPONENT_ANNOTATION,
        BEAN_METHOD,
        FACTORY_BEAN,
        UNKNOWN
    }
}
