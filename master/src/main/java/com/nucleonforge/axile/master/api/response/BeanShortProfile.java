package com.nucleonforge.axile.master.api.response;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Short profile of a given bean.
 *
 * @param beanName     The name of the bean.
 * @param scope        The scope of the bean.
 * @param className    The fully qualified class name of the bean.
 * @param aliases      The aliases of the given bean.
 * @param dependencies The list of dependencies of this bean (i.e. other beans that this bean depends on).
 * @param isLazyInit   Whether the bean is lazily instantiated or eagerly.
 * @param isPrimary    Whether the bean is marked with {@link BeanDefinition#isPrimary() primary marker}.
 * @param qualifiers   The list of {@link AutowireCandidateQualifier qualifiers} that are assigned to this bean.
 * @param beanSource   The source information describing how this bean was created and its origin type.
 *
 * @author Mikhail Polivakha
 */
public record BeanShortProfile(
        String beanName,
        String scope,
        String className,
        ProxyType proxyType,
        Set<String> aliases,
        Set<String> dependencies,
        boolean isPrimary,
        boolean isLazyInit,
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
         * We do not know the origin of this bean. As of now, beans registered programmatically via {@link BeanDefinitionRegistry}
         * or anything similar are going to reside here. It is going to be fixed in the future versions.
         */
        UNKNOWN
    }

    public sealed interface BeanSource permits BeanMethod, ComponentVariant, FactoryBean, UnknownBean {

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

    /**
     * The {@link BeanSource} for the {@link BeanOrigin#BEAN_METHOD}. Has an additional
     * {@link String} fields for {@link #methodName name of the method} that produced the
     * actual bean, and the name of the class, where this {@link #methodName method}
     * resides.
     *
     * @author Mikhail Polivakha
     */
    public record BeanMethod(@Nullable String enclosingClassName, String methodName) implements BeanSource {

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
