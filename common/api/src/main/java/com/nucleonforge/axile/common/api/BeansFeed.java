package com.nucleonforge.axile.common.api;

import java.util.Map;
import java.util.Set;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to beans actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/beans.html">Beans Endpoint</a>
 * @author Mikhail Polivakha
 */
public class BeansFeed {

    private Map<String, Context> context;

    public Map<String, Context> getContext() {
        return context;
    }

    public BeansFeed setContext(Map<String, Context> context) {
        this.context = context;
        return this;
    }

    public static class Context {

        private String parentId;
        private Map<String, Bean> beans;

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

        private Set<String> aliases;
        private String scope;
        private String type;
        private Set<String> dependencies;

        public Set<String> getAliases() {
            return aliases;
        }

        public Bean setAliases(Set<String> aliases) {
            this.aliases = aliases;
            return this;
        }

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

        public Bean setDependencies(Set<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Bean setDependencies(String... dependencies) {
            this.dependencies = Set.of(dependencies);
            return this;
        }
    }
}
