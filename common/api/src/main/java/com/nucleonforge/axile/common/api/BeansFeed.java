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

    public void setContext(Map<String, Context> context) {
        this.context = context;
    }

    public static class Context {

        private String parentId;
        private Map<String, Bean> beans;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public Map<String, Bean> getBeans() {
            return beans;
        }

        public void setBeans(Map<String, Bean> beans) {
            this.beans = beans;
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

        public void setAliases(Set<String> aliases) {
            this.aliases = aliases;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(Set<String> dependencies) {
            this.dependencies = dependencies;
        }
    }
}
