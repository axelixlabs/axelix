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

    private final List<Bean> beans;

    /**
     * Create new BeansFeed.
     *
     * @param beans  The unified list of beans that contains beans from one or more contexts.
     */
    @JsonCreator
    public BeansFeed(@JsonProperty("beans") List<Bean> beans) {
        this.beans = beans;
    }

    public List<Bean> getBeans() {
        return beans;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeansFeed that = (BeansFeed) o;
        return Objects.equals(beans, that.beans);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(beans);
    }

    @Override
    public String toString() {
        return "BeansFeed{" + "beans=" + beans + '}';
    }

    /**
     * The profile of a given bean.
     */
    public static final class Bean {
        private final String beanName;
        private final String className;
        private final String scope;
        private final ProxyType proxyType;
        private final Set<String> aliases;

        @Nullable
        private final String autoConfigurationRef;

        private final Set<BeanDependency> dependencies;

        @JsonProperty("isPrimary")
        private final boolean isPrimary;

        @JsonProperty("isLazyInit")
        private final boolean isLazyInit;

        @JsonProperty("isConfigPropsBean")
        private final boolean isConfigPropsBean;

        private final List<String> qualifiers;
        private final BeanSource beanSource;

        /**
         * Create a new Bean.
         *
         * @param beanName              The name of the bean.
         * @param scope                 The scope of the bean.
         * @param className             The fully qualified class name of the bean.
         * @param aliases               The aliases of the given bean.
         * @param autoConfigurationRef  The reference to an @AutoConfiguration class if it is annotated with conditions
         *                              {@code className}, or, if it contains a method annotated with conditions,
         *                              a reference to the class with the method specified {@code className#methodName}.
         * @param dependencies          The list of dependencies of this bean (i.e. other beans that this bean depends on).
         * @param isLazyInit            Whether the bean is lazily instantiated or eagerly.
         * @param isPrimary             Whether the bean is marked with BeanDefinition#isPrimary() primary marker.
         * @param isConfigPropsBean     Whether the bean is a configuration properties bean ConfigurationProperties.
         * @param qualifiers            The list of AutowireCandidateQualifier qualifiers that are assigned to this bean.
         * @param beanSource            The source information describing how this bean was created and its origin type.
         */
        @JsonCreator
        public Bean(
                @JsonProperty("beanName") String beanName,
                @JsonProperty("className") String className,
                @JsonProperty("scope") String scope,
                @JsonProperty("proxyType") ProxyType proxyType, // Нужно поменять на String
                @JsonProperty("aliases") Set<String> aliases,
                @JsonProperty("autoConfigurationRef") @Nullable String autoConfigurationRef,
                @JsonProperty("dependencies") Set<BeanDependency> dependencies,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("isLazyInit") boolean isLazyInit,
                @JsonProperty("isConfigPropsBean") boolean isConfigPropsBean,
                @JsonProperty("qualifiers") List<String> qualifiers,
                @JsonProperty("beanSource") @JsonDeserialize(using = BeanSourceDeserializer.class)
                        BeanSource beanSource) {
            this.beanName = beanName;
            this.className = className;
            this.scope = scope;
            this.proxyType = proxyType;
            this.aliases = aliases != null ? aliases : Collections.emptySet();
            this.autoConfigurationRef = autoConfigurationRef;
            this.dependencies = dependencies != null ? dependencies : Collections.emptySet();
            this.isPrimary = isPrimary;
            this.isLazyInit = isLazyInit;
            this.isConfigPropsBean = isConfigPropsBean;
            this.qualifiers = qualifiers != null ? qualifiers : Collections.emptyList();
            this.beanSource = beanSource;
        }

        public String getBeanName() {
            return beanName;
        }

        public String getClassName() {
            return className;
        }

        public String getScope() {
            return scope;
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

        @JsonProperty("isLazyInit")
        public boolean isLazyInit() {
            return isLazyInit;
        }

        @JsonProperty("isPrimary")
        public boolean isPrimary() {
            return isPrimary;
        }

        @JsonProperty("isConfigPropsBean")
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
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bean bean = (Bean) o;
            return isPrimary == bean.isPrimary
                    && isLazyInit == bean.isLazyInit
                    && isConfigPropsBean == bean.isConfigPropsBean
                    && Objects.equals(beanName, bean.beanName)
                    && Objects.equals(className, bean.className)
                    && Objects.equals(scope, bean.scope)
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
                    beanName,
                    className,
                    scope,
                    proxyType,
                    aliases,
                    autoConfigurationRef,
                    dependencies,
                    isPrimary,
                    isLazyInit,
                    isConfigPropsBean,
                    qualifiers,
                    beanSource);
        }

        @Override
        public String toString() {
            return "Bean{" + "beanName='"
                    + beanName + '\'' + ", className='"
                    + className + '\'' + ", scope='"
                    + scope + '\'' + ", proxyType="
                    + proxyType + ", aliases="
                    + aliases + ", autoConfigurationRef='"
                    + autoConfigurationRef + '\'' + ", dependencies="
                    + dependencies + ", isPrimary="
                    + isPrimary + ", isLazyInit="
                    + isLazyInit + ", isConfigPropsBean="
                    + isConfigPropsBean + ", qualifiers="
                    + qualifiers + ", beanSource="
                    + beanSource + '}';
        }
    }

    /**
     * The profile of a given bean dependency.
     */
    public static final class BeanDependency {

        private final String name;

        @JsonProperty("isConfigPropsDependency")
        private final boolean isConfigPropsDependency;

        /**
         * Create a new BeanDependency.
         *
         * @param name The name of the dependency bean.
         * @param isConfigPropsDependency Whether the dependency is a configuration properties dependency.
         */
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

        @JsonProperty("isConfigPropsDependency")
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
        /**
         * Bean originated from some variant of a @Component annotation.
         */
        COMPONENT_ANNOTATION,

        /**
         * Bean was produced as a result of the @Bean method invocation
         */
        BEAN_METHOD,

        /**
         * Bean was created by another bean, {@link FactoryBean "factory bean"}, for example Spring Data repositories are
         * typically registered in this way.
         */
        FACTORY_BEAN,

        /**
         * This is the "synthetic" bean. It means that, most likely, this Bean was created programmatically inside the
         * Spring Framework, but it might be also created programmatically by some library that uses Spring as well.
         */
        SYNTHETIC_BEAN,

        /**
         * We do not know the origin of this bean. As of now, beans registered programmatically via {BeanDefinitionRegistry
         * or anything similar are going to reside here. It is going to be fixed in the future versions.
         */
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

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#UNKNOWN}. Does not hold
     * any fields since the actual origin if unknown.
     */
    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class UnknownBean implements BeanSource {

        /**
         * Create a new UnknownBean.
         */
        @JsonCreator
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

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#FACTORY_BEAN}. Holds the
     * reference to the {@link #factoryBeanName factory bean} that produced this bean.
     */
    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class FactoryBean implements BeanSource {

        private final String factoryBeanName;

        /**
         * Create a new UnknownBean.
         *
         * @param factoryBeanName the reference to the {@link #factoryBeanName factory bean}.
         */
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

    /**
     * This is the "synthetic" bean. It means that, most likely, this Bean was created programmatically inside the
     * Spring Framework, but it might be also created programmatically by some library that uses Spring as well.
     */
    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class SyntheticBean implements BeanSource {

        /**
         * Create a new SyntheticBean.
         */
        @JsonCreator
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

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#BEAN_METHOD}. Has an additional
     * {@link String} fields for {@link #methodName name of the method} that produced the
     * actual bean, and the name of the class, where this {@link #methodName method}
     * resides.
     */
    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class BeanMethod implements BeanSource {

        @Nullable
        private final String enclosingClassName;

        @Nullable
        private final String enclosingClassFullName;

        private final String methodName;

        /**
         * Create a new BeanMethod.
         *
         * @param enclosingClassName        the class name where the bean is created.
         * @param enclosingClassFullName    the full class name where the bean is created.
         * @param methodName                the method name that creates a bean.
         */
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

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#COMPONENT_ANNOTATION}. Does not have
     * any additional fields since it is quite obvious by the #className bean class name
     * from where the class actually originated.
     */
    @JsonIgnoreProperties(value = BeanSourceDeserializer.ORIGIN_FIELD, allowGetters = true)
    public static final class ComponentVariant implements BeanSource {

        /**
         * Create a new ComponentVariant.
         */
        @JsonCreator
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
     */
    public enum ProxyType {
        /** Bean is proxied using JDK dynamic proxy */
        JDK_PROXY,

        /** Bean is proxied using CGLIB */
        CGLIB,

        /** Bean is not proxied */
        NO_PROXYING
    }
}
