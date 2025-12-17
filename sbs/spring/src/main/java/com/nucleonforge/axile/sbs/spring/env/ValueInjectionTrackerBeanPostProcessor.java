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
package com.nucleonforge.axile.sbs.spring.env;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed.InjectionPoint;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.InjectionType;

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
 */
public class ValueInjectionTrackerBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, List<InjectionPoint>> propertyToInjectionPoints = new ConcurrentHashMap<>();
    private final PropertyNameNormalizer propertyNameNormalizer;
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+?)(?::(.+?))?}");

    public ValueInjectionTrackerBeanPostProcessor(PropertyNameNormalizer propertyNameNormalizer) {
        this.propertyNameNormalizer = propertyNameNormalizer;
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
        Class<?> beanClass = bean.getClass();

        analyzeFields(beanClass, beanName);
        analyzeMethods(beanClass, beanName);
        analyzeConstructors(beanClass, beanName);
    }

    private void analyzeFields(Class<?> beanClass, String beanName) {
        ReflectionUtils.doWithFields(beanClass, field -> {
            Value valueAnnotation = findValueAnnotation(field);
            if (valueAnnotation != null) {
                processValueAnnotation(valueAnnotation, beanName, InjectionType.FIELD, field.getName());
            }
        });
    }

    private void analyzeMethods(Class<?> beanClass, String beanName) {
        ReflectionUtils.doWithMethods(beanClass, method -> {
            for (Parameter parameter : method.getParameters()) {
                Value parameterAnnotation = findValueAnnotation(parameter);
                if (parameterAnnotation != null) {
                    processValueAnnotation(
                            parameterAnnotation,
                            beanName,
                            InjectionType.METHOD_PARAMETER,
                            method.getName() + "::" + parameter.getName());
                }
            }

            Value methodAnnotation = findValueAnnotation(method);
            if (methodAnnotation != null) {
                processValueAnnotation(methodAnnotation, beanName, InjectionType.METHOD, method.getName());
            }
        });
    }

    private void analyzeConstructors(Class<?> beanClass, String beanName) {
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            for (Parameter parameter : constructor.getParameters()) {
                Value valueAnnotation = findValueAnnotation(parameter);
                if (valueAnnotation != null) {
                    processValueAnnotation(
                            valueAnnotation, beanName, InjectionType.CONSTRUCTOR_PARAMETER, parameter.getName());
                }
            }
        }
    }

    @Nullable
    private Value findValueAnnotation(AnnotatedElement element) {
        Value directAnnotation = element.getAnnotation(Value.class);
        if (directAnnotation != null) {
            return directAnnotation;
        }

        for (Annotation annotation : element.getAnnotations()) {
            Value metaAnnotation = annotation.annotationType().getAnnotation(Value.class);
            if (metaAnnotation != null) {
                return metaAnnotation;
            }
        }

        return null;
    }

    private void processValueAnnotation(
            Value annotation, String beanName, InjectionType injectionType, String targetName) {
        String expression = annotation.value();

        List<String> propertyNames = extractPropertyNamesFromExpression(expression);

        for (String propertyName : propertyNames) {
            String normalizedName = propertyNameNormalizer.normalize(propertyName);

            InjectionPoint injectionPoint = new InjectionPoint(beanName, injectionType, targetName, expression);

            propertyToInjectionPoints
                    .computeIfAbsent(normalizedName, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(injectionPoint);
        }
    }

    private List<String> extractPropertyNamesFromExpression(String expression) {
        List<String> propertyNames = new ArrayList<>();

        // ${property.name}
        Matcher matcher = PROPERTY_PATTERN.matcher(expression);
        while (matcher.find()) {
            String propertyExpression = matcher.group(1).trim();
            propertyNames.add(propertyExpression);
        }

        // SpEL
        if (expression.contains("#{")) {
            extractPropertyNamesFromSpEL(expression, propertyNames);
        }

        return propertyNames;
    }

    private void extractPropertyNamesFromSpEL(String expression, List<String> propertyNames) {
        // "#{environment.getProperty('server.port')}"
        Pattern spelPropertyPattern = Pattern.compile("getProperty\\s*\\(\\s*['\"]([^'\"]+)['\"]");
        Matcher matcher = spelPropertyPattern.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }

        // "#{systemProperties['server.port']}"
        Pattern spelBracketPattern = Pattern.compile("\\[\\s*['\"]([^'\"]+)['\"]\\s*]");
        matcher = spelBracketPattern.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }
    }
}
