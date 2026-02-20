/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.common.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.Nullable;

/**
 * The response to beans actuator endpoint.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public final class BeansFeed {

    private final Map<String, Context> contexts;

    @JsonCreator
    public BeansFeed(@JsonProperty("contexts") Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeansFeed beansFeed = (BeansFeed) o;
        return Objects.equals(contexts, beansFeed.contexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contexts);
    }

    @Override
    public String toString() {
        return "BeansFeed{" + "contexts=" + contexts + '}';
    }

    public static final class Context {

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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Context context = (Context) o;
            return Objects.equals(parentId, context.parentId) && Objects.equals(beans, context.beans);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentId, beans);
        }

        @Override
        public String toString() {
            return "Context{" + "parentId='" + parentId + '\'' + ", beans=" + beans + '}';
        }
    }

    public static final class Bean {

        private final String scope;
        private final String type;
        private final ProxyType proxyType;
        private final Set<String> aliases;

        @Nullable
        private final String autoConfigurationRef;

        private final Set<BeanDependency> dependencies;

        @JsonProperty("isLazyInit")
        private final boolean isLazyInit;

        @JsonProperty("isPrimary")
        private final boolean isPrimary;

        @JsonProperty("isConfigPropsBean")
        private final boolean isConfigPropsBean;

        private final List<String> qualifiers;
        private final BeanSource beanSource;

        @JsonCreator
        public Bean(
                @JsonProperty("scope") String scope,
                @JsonProperty("type") String type,
                @JsonProperty("proxyType") ProxyType proxyType,
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("autoConfigurationRef") @Nullable String autoConfigurationRef,
                @JsonProperty("dependencies") Set<BeanDependency> dependencies,
                @JsonProperty("isLazyInit") boolean isLazyInit,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("isConfigPropsBean") boolean isConfigPropsBean,
                @JsonProperty("qualifiers") List<String> qualifiers,
                @JsonProperty("beanSource") @JsonDeserialize(using = BeanSourceDeserializer.class)
                        BeanSource beanSource) {
            this.scope = scope;
            this.type = type;
            this.proxyType = proxyType;
            this.aliases = aliases != null ? aliases : Collections.emptySet();
            this.autoConfigurationRef = autoConfigurationRef;
            this.dependencies = dependencies != null ? dependencies : Collections.emptySet();
            this.isLazyInit = isLazyInit;
            this.isPrimary = isPrimary;
            this.isConfigPropsBean = isConfigPropsBean;
            this.qualifiers = qualifiers != null ? qualifiers : Collections.emptyList();
            this.beanSource = beanSource;
        }

        public String getScope() {
            return scope;
        }

        public String getType() {
            return type;
        }

        public ProxyType getProxyType() {
            return proxyType;
        }

        public Set<String> getAliases() {
            return aliases;
        }

        @Nullable
        public String getAutoConfigurationRef() {
            return autoConfigurationRef;
        }

        public Set<BeanDependency> getDependencies() {
            return dependencies;
        }

        public boolean isLazyInit() {
            return isLazyInit;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public boolean isConfigPropsBean() {
            return isConfigPropsBean;
        }

        public List<String> getQualifiers() {
            return qualifiers;
        }

        public BeanSource getBeanSource() {
            return beanSource;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bean bean = (Bean) o;
            return isLazyInit == bean.isLazyInit
                    && isPrimary == bean.isPrimary
                    && isConfigPropsBean == bean.isConfigPropsBean
                    && Objects.equals(scope, bean.scope)
                    && Objects.equals(type, bean.type)
                    && proxyType == bean.proxyType
                    && Objects.equals(aliases, bean.aliases)
                    && Objects.equals(autoConfigurationRef, bean.autoConfigurationRef)
                    && Objects.equals(dependencies, bean.dependencies)
                    && Objects.equals(qualifiers, bean.qualifiers)
                    && Objects.equals(beanSource, bean.beanSource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    scope,
                    type,
                    proxyType,
                    aliases,
                    autoConfigurationRef,
                    dependencies,
                    isLazyInit,
                    isPrimary,
                    isConfigPropsBean,
                    qualifiers,
                    beanSource);
        }

        @Override
        public String toString() {
            return "Bean{"
                    + "scope='"
                    + scope
                    + '\''
                    + ", type='"
                    + type
                    + '\''
                    + ", proxyType="
                    + proxyType
                    + ", aliases="
                    + aliases
                    + ", autoConfigurationRef='"
                    + autoConfigurationRef
                    + '\''
                    + ", dependencies="
                    + dependencies
                    + ", isLazyInit="
                    + isLazyInit
                    + ", isPrimary="
                    + isPrimary
                    + ", isConfigPropsBean="
                    + isConfigPropsBean
                    + ", qualifiers="
                    + qualifiers
                    + ", beanSource="
                    + beanSource
                    + '}';
        }
    }

    public static final class BeanDependency {

        private final String name;

        @JsonProperty("isConfigPropsDependency")
        private final boolean isConfigPropsDependency;

        @JsonCreator
        public BeanDependency(
                @JsonProperty("name") String name,
                @JsonProperty("isConfigPropsDependency") boolean isConfigPropsDependency) {
            this.name = name;
            this.isConfigPropsDependency = isConfigPropsDependency;
        }

        public String getName() {
            return name;
        }

        public boolean isConfigPropsDependency() {
            return isConfigPropsDependency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BeanDependency that = (BeanDependency) o;
            return isConfigPropsDependency == that.isConfigPropsDependency && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, isConfigPropsDependency);
        }

        @Override
        public String toString() {
            return "BeanDependency{" + "name='" + name + '\'' + ", isConfigPropsDependency=" + isConfigPropsDependency
                    + '}';
        }
    }

    public enum BeanOrigin {
        COMPONENT_ANNOTATION,
        BEAN_METHOD,
        FACTORY_BEAN,
        SYNTHETIC_BEAN,
        UNKNOWN,
    }

    /**
     * Interface representing bean source information.
     * In Java 11, we use a regular interface instead of sealed interface.
     */
    public interface BeanSource {

        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        BeanOrigin origin();
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class UnknownBean implements BeanSource {

        public UnknownBean() {}

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.UNKNOWN;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(origin());
        }

        @Override
        public String toString() {
            return "UnknownBean{}";
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class FactoryBean implements BeanSource {

        private final String factoryBeanName;

        @JsonCreator
        public FactoryBean(@JsonProperty("factoryBeanName") String factoryBeanName) {
            this.factoryBeanName = factoryBeanName;
        }

        public String getFactoryBeanName() {
            return factoryBeanName;
        }

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.FACTORY_BEAN;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FactoryBean that = (FactoryBean) o;
            return Objects.equals(factoryBeanName, that.factoryBeanName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(factoryBeanName);
        }

        @Override
        public String toString() {
            return "FactoryBean{" + "factoryBeanName='" + factoryBeanName + '\'' + '}';
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class SyntheticBean implements BeanSource {

        public SyntheticBean() {}

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.SYNTHETIC_BEAN;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(origin());
        }

        @Override
        public String toString() {
            return "SyntheticBean{}";
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class BeanMethod implements BeanSource {

        @Nullable
        private final String enclosingClassName;

        @Nullable
        private final String enclosingClassFullName;

        private final String methodName;

        @JsonCreator
        public BeanMethod(
                @JsonProperty("enclosingClassName") @Nullable String enclosingClassName,
                @JsonProperty("enclosingClassFullName") @Nullable String enclosingClassFullName,
                @JsonProperty("methodName") String methodName) {
            this.enclosingClassName = enclosingClassName;
            this.enclosingClassFullName = enclosingClassFullName;
            this.methodName = methodName;
        }

        @Nullable
        public String getEnclosingClassName() {
            return enclosingClassName;
        }

        @Nullable
        public String getEnclosingClassFullName() {
            return enclosingClassFullName;
        }

        public String getMethodName() {
            return methodName;
        }

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.BEAN_METHOD;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BeanMethod that = (BeanMethod) o;
            return Objects.equals(enclosingClassName, that.enclosingClassName)
                    && Objects.equals(enclosingClassFullName, that.enclosingClassFullName)
                    && Objects.equals(methodName, that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enclosingClassName, enclosingClassFullName, methodName);
        }

        @Override
        public String toString() {
            return "BeanMethod{"
                    + "enclosingClassName='"
                    + enclosingClassName
                    + '\''
                    + ", enclosingClassFullName='"
                    + enclosingClassFullName
                    + '\''
                    + ", methodName='"
                    + methodName
                    + '\''
                    + '}';
        }
    }

    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class ComponentVariant implements BeanSource {

        public ComponentVariant() {}

        @Override
        @JsonGetter(BeanSourceDeserializer.ORIGIN_FIELD)
        public BeanOrigin origin() {
            return BeanOrigin.COMPONENT_ANNOTATION;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(origin());
        }

        @Override
        public String toString() {
            return "ComponentVariant{}";
        }
    }

    /**
     * The proxying approach that has been applied for the given bean.
     *
     * @author Nikita Kirillov
     */
    public enum ProxyType {
        JDK_PROXY,
        CGLIB,
        NO_PROXYING
    }
}
