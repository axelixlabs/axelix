/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.nucleonforge.axile.common.api.BeansFeed;

/**
 * Default implementation of {@link BeanMetaInfoExtractor}.
 *
 * @author Nikita Kirillov
 * @author Sergey  Cherkasov
 * @since 04.07.2025
 */
@NullMarked
public class DefaultBeanMetaInfoExtractor implements BeanMetaInfoExtractor {

    private final DefaultQualifiersRegistry qualifiersRegistry;
    private final ConfigurableListableBeanFactory beanFactory;
    private final ConditionsReportEndpoint delegateConditions;
    private final BeanNameNormalizer beanNameNormalizer;

    public DefaultBeanMetaInfoExtractor(
            ConfigurableListableBeanFactory configurableBeanFactory,
            ConditionsReportEndpoint delegateConditions,
            BeanNameNormalizer beanNameNormalizer) {
        this.beanFactory = configurableBeanFactory;
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
        this.delegateConditions = delegateConditions;
        this.beanNameNormalizer = beanNameNormalizer;
    }

    @Override
    public BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        Object bean = beanFactory.getBean(beanName);
        Map<String, List<String>> positiveConditionsMapping = conditionsMappingMap();

        return new BeanMetaInfo(
                getConditionRef(positiveConditionsMapping, beanDefinition, beanName),
                analyzeProxyType(bean.getClass()),
                beanDefinition.isLazyInit(),
                beanDefinition.isPrimary(),
                qualifiersRegistry.getQualifiers(beanName),
                analyzeBeanSource(beanDefinition, beanName));
    }

    private BeansFeed.ProxyType analyzeProxyType(Class<?> beanType) {
        if (Proxy.isProxyClass(beanType)) {
            return BeansFeed.ProxyType.JDK_PROXY;
        } else if (beanType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR) && !beanType.isHidden()) {
            return BeansFeed.ProxyType.CGLIB;
        }
        return BeansFeed.ProxyType.NO_PROXYING;
    }

    private BeansFeed.BeanSource analyzeBeanSource(BeanDefinition beanDefinition, String beanName) {
        if (beanDefinition.getFactoryMethodName() != null) {
            Class<?> enclosingClass = extractEnclosingClass(beanDefinition, beanName);

            return new BeansFeed.BeanMethod(
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
                return new BeansFeed.ComponentVariant();
            }
        }

        if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
            if (abstractBeanDefinition.isSynthetic()) {
                return new BeansFeed.SyntheticBean();
            }
        }

        return new BeansFeed.UnknownBean();
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

    private Map<String, List<String>> conditionsMappingMap() {
        Map<String, List<String>> map = new HashMap<>();

        delegateConditions.conditions().getContexts().values().forEach(context -> {
            if (context.getPositiveMatches() != null) {
                for (var entry : context.getPositiveMatches().entrySet()) {
                    unwrapConditionBeanName(entry.getKey(), map);
                }
            }
        });

        return map;
    }

    private static void unwrapConditionBeanName(String target, Map<String, List<String>> map) {
        if (target.contains("#")) {
            String[] pair = target.split("#", 2);
            map.computeIfAbsent(pair[0], k -> new ArrayList<>()).add(pair[1]);
        } else {
            map.computeIfAbsent(target, k -> new ArrayList<>()).add(null);
        }
    }

    @Nullable
    private String getConditionRef(
            Map<String, List<String>> positiveConditionsMapping, BeanDefinition beanDefinition, String beanName) {
        if (beanDefinition.getFactoryMethodName() != null) {
            Class<?> enclosingClass = extractEnclosingClass(beanDefinition, beanName);

            String enclosingClassName = Optional.ofNullable(enclosingClass)
                    .map(ClassUtils::getUserClass)
                    .map(Class::getName)
                    .orElse(null);

            return buildConditionRef(
                    positiveConditionsMapping,
                    beanNameNormalizer.normalize(enclosingClassName),
                    beanDefinition.getFactoryMethodName());
        }

        String normalizedBeanName = beanNameNormalizer.normalize(beanName);

        return positiveConditionsMapping.containsKey(normalizedBeanName) ? normalizedBeanName : null;
    }

    @Nullable
    private String buildConditionRef(
            Map<String, List<String>> positiveConditionsMapping, @Nullable String beanName, String methodName) {
        return Optional.ofNullable(beanName)
                .flatMap(name -> Optional.ofNullable(positiveConditionsMapping.get(name))
                        .map(methods -> methods.contains(methodName) ? name + "#" + methodName : name))
                .orElse(null);
    }
}
