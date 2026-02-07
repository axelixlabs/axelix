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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Short profile of a given bean.
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
 * @param isPrimary             Whether the bean is marked with {@link BeanDefinition#isPrimary() primary marker}.
 * @param isConfigPropsBean     Whether the bean is a configuration properties bean {@link ConfigurationProperties}.
 * @param qualifiers            The list of {@link AutowireCandidateQualifier qualifiers} that are assigned to this bean.
 * @param beanSource            The source information describing how this bean was created and its origin type.
 *
 * @author Mikhail Polivakha
 * @author Sergey  Cherkasov
 */
public record BeanShortProfile(
        String beanName,
        String scope,
        String className,
        ProxyType proxyType,
        Set<String> aliases,
        @Nullable String autoConfigurationRef,
        Set<BeanDependencyProfile> dependencies,
        boolean isPrimary,
        boolean isLazyInit,
        boolean isConfigPropsBean,
        List<String> qualifiers,
        BeanSource beanSource) {

    public BeanShortProfile {
        if (aliases == null) {
            aliases = Collections.emptySet();
        }
        if (dependencies == null) {
            dependencies = Collections.emptySet();
        }
        if (qualifiers == null) {
            qualifiers = Collections.emptyList();
        }
    }

    /**
     * @param name The name of the dependency bean.
     * @param isConfigPropsDependency Whether the dependency is a configuration properties dependency.
     */
    public record BeanDependencyProfile(String name, boolean isConfigPropsDependency) {}

    public enum BeanOrigin {

        /**
         * Bean originated from some variant of a @{@link Component} annotation.
         */
        COMPONENT_ANNOTATION,

        /**
         * Bean was produced as a result of the @{@link Bean @Bean} method invocation
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
         * We do not know the origin of this bean. As of now, beans registered programmatically via {@link BeanDefinitionRegistry}
         * or anything similar are going to reside here. It is going to be fixed in the future versions.
         */
        UNKNOWN
    }

    public sealed interface BeanSource permits BeanMethod, ComponentVariant, FactoryBean, SyntheticBean, UnknownBean {

        @JsonGetter("origin")
        BeanOrigin origin();
    }

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#UNKNOWN}. Does not hold
     * any fields since the actual origin if unknown.
     *
     * @author Mikhail Polivakha
     */
    public record UnknownBean() implements BeanSource {

        @Override
        public BeanOrigin origin() {
            return BeanOrigin.UNKNOWN;
        }
    }

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#FACTORY_BEAN}. Holds the
     * reference to the {@link #factoryBeanName factory bean} that produced this bean.
     *
     * @author Mikhail Polivakha
     */
    public record FactoryBean(String factoryBeanName) implements BeanSource {

        @Override
        public BeanOrigin origin() {
            return BeanOrigin.FACTORY_BEAN;
        }
    }

    public record SyntheticBean() implements BeanSource {

        @Override
        public BeanOrigin origin() {
            return BeanOrigin.SYNTHETIC_BEAN;
        }
    }

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#BEAN_METHOD}. Has an additional
     * {@link String} fields for {@link #methodName name of the method} that produced the
     * actual bean, and the name of the class, where this {@link #methodName method}
     * resides.
     *
     * @author Mikhail Polivakha
     */
    public record BeanMethod(
            @Nullable String enclosingClassName, @Nullable String enclosingClassFullName, String methodName)
            implements BeanSource {

        @Override
        public BeanOrigin origin() {
            return BeanOrigin.BEAN_METHOD;
        }
    }

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#COMPONENT_ANNOTATION}. Does not have
     * any additional fields since it is quite obvious by the {@link #className bean class name}
     * from where the class actually originated.
     *
     * @author Mikhail Polivakha
     */
    public record ComponentVariant() implements BeanSource {

        @Override
        public BeanOrigin origin() {
            return BeanOrigin.COMPONENT_ANNOTATION;
        }
    }

    /**
     * Enum representing the type of proxy applied to a bean.
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
