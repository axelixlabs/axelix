package com.nucleonforge.axile.master.api.response;

import java.util.Set;

/**
 * Short profile of a given bean.
 *
 * @author Mikhail Polivakha
 */
public class BeanShortProfile {

    /**
     * The name of the bean.
     */
    private String beanName;

    /**
     * The scope of the bean.
     */
    private String scope;

    /**
     * The fully qualified class named of the bean.
     */
    private String className;

    /**
     * The aliases of the given bean.
     */
    private Set<String> aliases;

    /**
     * The list of dependencies of this bean (i.e. other beans that this bean depends on).
     */
    private Set<String> dependencies;

    public BeanShortProfile(
            String beanName, String scope, String className, Set<String> aliases, Set<String> dependencies) {
        this.beanName = beanName;
        this.scope = scope;
        this.className = className;
        this.aliases = aliases;
        this.dependencies = dependencies;
    }

    public String getBeanName() {
        return beanName;
    }

    public BeanShortProfile setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public BeanShortProfile setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public BeanShortProfile setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public BeanShortProfile setAliases(Set<String> aliases) {
        if (aliases != null) {
            this.aliases = aliases;
        }
        return this;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public BeanShortProfile setDependencies(Set<String> dependencies) {
        if (dependencies != null) {
            this.dependencies = dependencies;
        }
        return this;
    }
}
