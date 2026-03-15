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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ReflectionUtils;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;

/**
 * Tracks all @Value injections in Spring beans.
 * <p>
 * This BeanPostProcessor analyzes beans during Spring initialization to detect
 * all points where values are injected via @Value annotations or custom annotations
 * meta-annotated with @Value.
 * <p>
 * All detected injection points are stored and can be retrieved by normalized property name.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class ValueInjectionTrackerBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, List<InjectionPoint>> propertyToInjectionPoints = new ConcurrentHashMap<>();
    private final ValueAnnotationInjectionProcessor annotationInjectionProcessor;

    public ValueInjectionTrackerBeanPostProcessor(ValueAnnotationInjectionProcessor annotationInjectionProcessor) {
        this.annotationInjectionProcessor = annotationInjectionProcessor;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
        analyzeBean(bean, beanName);
        return bean;
    }

    @Nullable
    public List<InjectionPoint> getInjectionPointsForProperty(String propertyName) {
        return propertyToInjectionPoints.get(propertyName);
    }

    private void analyzeBean(Object bean, String beanName) {
        // At this point it is safe to just call getClass() since bean is not proxied yet
        Class<?> beanClass = bean.getClass();

        analyzeFields(beanClass, beanName);
        analyzeMethods(beanClass, beanName);
        analyzeConstructors(beanClass, beanName);
    }

    private void analyzeFields(Class<?> beanClass, String beanName) {
        ReflectionUtils.doWithFields(beanClass, field -> {
            Value valueAnnotation = findValueAnnotation(field);
            if (valueAnnotation != null) {
                annotationInjectionProcessor.processValueAnnotation(
                        propertyToInjectionPoints,
                        valueAnnotation.value(),
                        beanName,
                        InjectionType.FIELD,
                        field.getName());
            }
        });
    }

    private void analyzeMethods(Class<?> beanClass, String beanName) {
        ReflectionUtils.doWithMethods(beanClass, method -> {
            for (Parameter parameter : method.getParameters()) {
                Value parameterAnnotation = findValueAnnotation(parameter);
                if (parameterAnnotation != null) {
                    annotationInjectionProcessor.processValueAnnotation(
                            propertyToInjectionPoints,
                            parameterAnnotation.value(),
                            beanName,
                            InjectionType.METHOD_PARAMETER,
                            method.getName() + "::" + parameter.getName());
                }
            }

            Value methodAnnotation = findValueAnnotation(method);
            if (methodAnnotation != null) {
                annotationInjectionProcessor.processValueAnnotation(
                        propertyToInjectionPoints,
                        methodAnnotation.value(),
                        beanName,
                        InjectionType.METHOD,
                        method.getName());
            }
        });
    }

    private void analyzeConstructors(Class<?> beanClass, String beanName) {
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            for (Parameter parameter : constructor.getParameters()) {
                Value valueAnnotation = findValueAnnotation(parameter);
                if (valueAnnotation != null) {
                    annotationInjectionProcessor.processValueAnnotation(
                            propertyToInjectionPoints,
                            valueAnnotation.value(),
                            beanName,
                            InjectionType.CONSTRUCTOR_PARAMETER,
                            parameter.getName());
                }
            }
        }
    }

    @Nullable
    private Value findValueAnnotation(AnnotatedElement element) {
        return MergedAnnotations.from(element)
                .get(Value.class)
                .synthesize(MergedAnnotation::isPresent)
                .orElse(null);
    }
}
