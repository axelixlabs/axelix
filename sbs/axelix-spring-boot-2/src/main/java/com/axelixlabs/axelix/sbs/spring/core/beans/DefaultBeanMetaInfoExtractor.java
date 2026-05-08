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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.AutoConditionsRef;
import com.axelixlabs.axelix.common.api.ConditionsFeed;
import com.axelixlabs.axelix.sbs.spring.core.conditions.ConditionalFeedBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.ProxyUtils;

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

    @Nullable
    private final List<ConditionsFeed.PositiveCondition> positiveConditions;

    public DefaultBeanMetaInfoExtractor(
            ConfigurableApplicationContext configurableApplicationContext,
            ObjectProvider<ConditionalFeedBuilder> conditionalFeedBuilder) {
        this.beanFactory = configurableApplicationContext.getBeanFactory();
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
        ConditionalFeedBuilder builder = conditionalFeedBuilder.getIfAvailable();
        this.positiveConditions =
                builder == null ? null : builder.buildConditionsFeed().getPositiveMatches();
    }

    @Override
    public BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        Object bean = beanFactory.getBean(beanName);
        Class<?> beanType = bean.getClass();
        ProxyType beanProxyingType = ProxyUtils.analyzeProxyType(beanType, beanType.isSynthetic());
        BeanSource beanSource = analyzeBeanSource(beanDefinition, beanName);

        return new BeanMetaInfo(
                buildAutoConditionRef(beanDefinition, beanSource, beanName, bean),
                beanProxyingType,
                beanDefinition.isLazyInit(),
                beanDefinition.isPrimary(),
                qualifiersRegistry.getQualifiers(beanName),
                beanSource);
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

        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedDef = (AnnotatedBeanDefinition) beanDefinition;
            AnnotationMetadata metadata = annotatedDef.getMetadata();

            var mergedComponentAnnotation = metadata.getAnnotations().get(Component.class);

            if (mergedComponentAnnotation.isPresent()) {
                return new ComponentVariant();
            }
        }

        if (beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) beanDefinition;
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

        if (source instanceof StandardMethodMetadata) {
            StandardMethodMetadata metadata = (StandardMethodMetadata) source;
            Method introspectedMethod = metadata.getIntrospectedMethod();
            return introspectedMethod.getDeclaringClass();
        } else if (source instanceof MethodMetadata) {
            MethodMetadata metadata = (MethodMetadata) source;
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

    @Nullable
    private AutoConditionsRef buildAutoConditionRef(
            BeanDefinition beanDefinition, BeanSource beanSource, String beanName, Object bean) {

        if (positiveConditions == null) {
            return null;
        }

        Class<?> aClass = beanSource.origin() == BeansFeed.BeanOrigin.BEAN_METHOD
                ? extractEnclosingClass(beanDefinition, beanName)
                : bean.getClass();

        if (aClass == null) {
            return null;
        }

        String shortName = ClassUtils.getShortName(ProxyUtils.resolveUserClass(aClass));

        return positiveConditions.stream()
                .filter(condition -> condition.getClassName().equals(shortName))
                .filter(condition -> Objects.equals(condition.getMethodName(), beanDefinition.getFactoryMethodName()))
                .map(condition -> new AutoConditionsRef(condition.getClassName(), condition.getMethodName()))
                .findFirst()
                .orElse(null);
    }
}
