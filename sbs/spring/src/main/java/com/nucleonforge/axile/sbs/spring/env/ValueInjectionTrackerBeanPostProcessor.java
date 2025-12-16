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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;

/**
 * Tracks all @Value injections in Spring beans.
 * <p>
 *  This BeanPostProcessor analyzes beans during Spring initialization to detect
 *  all points where values are injected via @Value annotations or custom annotations
 *  meta-annotated with @Value.
 *
 *  All detected injection points are stored and can be retrieved by normalized property name.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 */
public class ValueInjectionTrackerBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, List<EnvironmentFeed.InjectionPoint>> propertyToInjectionPoints =
            new ConcurrentHashMap<>();
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

    private void analyzeBean(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();

        try {
            analyzeFields(beanClass, beanName);
            analyzeMethods(beanClass, beanName);
            analyzeConstructors(beanClass, beanName);
        } catch (Exception ignored) {
        }
    }

    private void analyzeFields(Class<?> beanClass, String beanName) {
        Class<?> currentClass = beanClass;

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                if (valueAnnotation != null) {
                    processValueAnnotation(
                            valueAnnotation, beanName, EnvironmentFeed.InjectionType.FIELD, field.getName());
                }

                Value metaValue = findMetaValueAnnotation(field);
                if (metaValue != null) {
                    processValueAnnotation(metaValue, beanName, EnvironmentFeed.InjectionType.FIELD, field.getName());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void analyzeMethods(Class<?> beanClass, String beanName) {
        Class<?> currentClass = beanClass;

        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    Value valueAnnotation = parameter.getAnnotation(Value.class);
                    if (valueAnnotation != null) {
                        String parameterName = parameter.getName();
                        String targetName = method.getName() + "::" + parameterName;

                        processValueAnnotation(
                                valueAnnotation, beanName, EnvironmentFeed.InjectionType.METHOD_PARAMETER, targetName);
                    }

                    Value metaValue = findMetaValueAnnotation(parameter);
                    if (metaValue != null) {
                        String parameterName = parameter.getName();
                        String targetName = method.getName() + "::" + parameterName;

                        processValueAnnotation(
                                metaValue, beanName, EnvironmentFeed.InjectionType.METHOD_PARAMETER, targetName);
                    }
                }

                Value methodValueAnnotation = method.getAnnotation(Value.class);
                if (methodValueAnnotation != null) {
                    processValueAnnotation(
                            methodValueAnnotation, beanName, EnvironmentFeed.InjectionType.METHOD, method.getName());
                }

                Value metaValue = findMetaValueAnnotation(method);
                if (metaValue != null) {
                    processValueAnnotation(metaValue, beanName, EnvironmentFeed.InjectionType.METHOD, method.getName());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void analyzeConstructors(Class<?> beanClass, String beanName) {
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            Parameter[] parameters = constructor.getParameters();

            for (Parameter parameter : parameters) {
                Value valueAnnotation = parameter.getAnnotation(Value.class);
                if (valueAnnotation != null) {
                    String paramName = parameter.getName();
                    processValueAnnotation(
                            valueAnnotation, beanName, EnvironmentFeed.InjectionType.CONSTRUCTOR_PARAMETER, paramName);
                }

                Value metaValue = findMetaValueAnnotation(parameter);
                if (metaValue != null) {
                    String paramName = parameter.getName();
                    processValueAnnotation(
                            metaValue, beanName, EnvironmentFeed.InjectionType.CONSTRUCTOR_PARAMETER, paramName);
                }
            }
        }
    }

    @Nullable
    private Value findMetaValueAnnotation(AnnotatedElement element) {
        Annotation[] annotations = element.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof Value) {
                continue;
            }

            Value metaValue = annotation.annotationType().getAnnotation(Value.class);
            if (metaValue != null) {
                return metaValue;
            }
        }

        return null;
    }

    private void processValueAnnotation(
            Value annotation, String beanName, EnvironmentFeed.InjectionType injectionType, String targetName) {
        String expression = annotation.value();

        List<String> propertyNames = extractPropertyNamesFromExpression(expression);

        for (String propertyName : propertyNames) {
            String normalizedName = propertyNameNormalizer.normalize(propertyName);

            EnvironmentFeed.InjectionPoint injectionPoint =
                    new EnvironmentFeed.InjectionPoint(beanName, injectionType, targetName, expression);

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

    public List<EnvironmentFeed.InjectionPoint> getInjectionPointsForProperty(String propertyName) {
        String normalized = propertyNameNormalizer.normalize(propertyName);
        List<EnvironmentFeed.InjectionPoint> points = propertyToInjectionPoints.get(normalized);
        return points != null ? new ArrayList<>(points) : Collections.emptyList();
    }
}
