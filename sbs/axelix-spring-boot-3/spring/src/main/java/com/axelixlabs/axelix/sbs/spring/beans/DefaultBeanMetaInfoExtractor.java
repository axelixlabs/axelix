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
package com.axelixlabs.axelix.sbs.spring.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.sbs.spring.conditions.ConditionalBeanRefBuilder;

import static com.axelixlabs.axelix.common.api.BeansFeed.BeanMethod;
import static com.axelixlabs.axelix.common.api.BeansFeed.BeanSource;
import static com.axelixlabs.axelix.common.api.BeansFeed.ComponentVariant;
import static com.axelixlabs.axelix.common.api.BeansFeed.ProxyType;
import static com.axelixlabs.axelix.common.api.BeansFeed.SyntheticBean;
import static com.axelixlabs.axelix.common.api.BeansFeed.UnknownBean;

/**
 * Default implementation of {@link BeanMetaInfoExtractor}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @since 04.07.2025
 */
@NullMarked
public class DefaultBeanMetaInfoExtractor implements BeanMetaInfoExtractor {

    private final DefaultQualifiersRegistry qualifiersRegistry;
    private final ConfigurableListableBeanFactory beanFactory;
    private final ConditionsReportEndpoint delegateConditions;
    private final ConditionalBeanRefBuilder conditionalBeanRefBuilder;

    public DefaultBeanMetaInfoExtractor(
            ConfigurableApplicationContext configurableApplicationContext,
            ConditionalBeanRefBuilder conditionalBeanRefBuilder) {
        this.beanFactory = configurableApplicationContext.getBeanFactory();
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
        this.delegateConditions = new ConditionsReportEndpoint(configurableApplicationContext);
        this.conditionalBeanRefBuilder = conditionalBeanRefBuilder;
    }

    @Override
    public BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        Object bean = beanFactory.getBean(beanName);
        Set<String> positiveConditionsKeys = conditionsKeys();
        ProxyType beanProxyingType = analyzeProxyType(bean.getClass());
        BeanSource beanSource = analyzeBeanSource(beanDefinition, beanName);

        return new BeanMetaInfo(
                getConditionRef(positiveConditionsKeys, beanDefinition, beanSource, beanName, bean),
                beanProxyingType,
                beanDefinition.isLazyInit(),
                beanDefinition.isPrimary(),
                qualifiersRegistry.getQualifiers(beanName),
                beanSource);
    }

    private ProxyType analyzeProxyType(Class<?> beanType) {
        if (Proxy.isProxyClass(beanType)) {
            return ProxyType.JDK_PROXY;
        } else if (beanType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR) && !beanType.isHidden()) {
            return ProxyType.CGLIB;
        }
        return ProxyType.NO_PROXYING;
    }

    private BeanSource analyzeBeanSource(BeanDefinition beanDefinition, String beanName) {
        if (beanDefinition.getFactoryMethodName() != null) {
            Class<?> enclosingClass = extractEnclosingClass(beanDefinition, beanName);

            return new BeanMethod(
                    Optional.ofNullable(enclosingClass)
                            .map(ClassUtils::getUserClass)
                            .map(Class::getSimpleName)
                            .orElse(null),
                    Optional.ofNullable(enclosingClass)
                            .map(ClassUtils::getUserClass)
                            .map(Class::getName)
                            .orElse(null),
                    beanDefinition.getFactoryMethodName());
        }

        if (beanDefinition.getBeanClassName() != null && isFactoryBeanClass(beanDefinition.getBeanClassName())) {
            return new BeansFeed.FactoryBean(beanDefinition.getBeanClassName());
        }

        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedDef) {
            AnnotationMetadata metadata = annotatedDef.getMetadata();

            var mergedComponentAnnotation = metadata.getAnnotations().get(Component.class);

            if (mergedComponentAnnotation.isPresent()) {
                return new ComponentVariant();
            }
        }

        if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
            if (abstractBeanDefinition.isSynthetic()) {
                return new SyntheticBean();
            }
        }

        return new UnknownBean();
    }

    @Nullable
    private Class<?> extractEnclosingClass(BeanDefinition beanDefinition, String beanName) {
        Class<?> result = extractClassFromSource(beanDefinition.getSource());

        if (result == null) {
            try {
                result = beanFactory.getType(beanName);
                if (Proxy.isProxyClass(result)) {
                    result = Class.forName(beanDefinition.getBeanClassName());
                }
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    @Nullable
    private Class<?> extractClassFromSource(@Nullable Object source) {
        if (source == null) {
            return null;
        }

        if (source instanceof StandardMethodMetadata metadata) {
            Method introspectedMethod = metadata.getIntrospectedMethod();
            return introspectedMethod.getDeclaringClass();
        } else if (source instanceof MethodMetadata metadata) {
            try {
                return Class.forName(metadata.getDeclaringClassName());
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    private boolean isFactoryBeanClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return FactoryBean.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Set<String> conditionsKeys() {
        return delegateConditions.conditions().getContexts().values().stream()
                .flatMap(context -> {
                    var positiveMatches = context.getPositiveMatches();

                    if (positiveMatches != null) {
                        return positiveMatches.keySet().stream();
                    } else {
                        return Stream.of();
                    }
                })
                .collect(Collectors.toSet());
    }

    @Nullable
    private String getConditionRef(
            Set<String> positiveConditionsKeys,
            BeanDefinition beanDefinition,
            BeanSource beanSource,
            String beanName,
            Object bean) {

        Class<?> configPropsTarget;

        if (beanSource.origin() == BeansFeed.BeanOrigin.BEAN_METHOD) {
            configPropsTarget = extractEnclosingClass(beanDefinition, beanName);
        } else {
            configPropsTarget = bean.getClass();
        }

        String normalizedBeanName =
                conditionalBeanRefBuilder.buildBeanRef(configPropsTarget, beanDefinition.getFactoryMethodName());

        if (positiveConditionsKeys.contains(normalizedBeanName)) {
            return normalizedBeanName;
        } else {
            return null;
        }
    }
}
