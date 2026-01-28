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
package com.axelixlabs.axelix.sbs.spring.env;

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
    private final PropertyNameNormalizer propertyNameNormalizer;

    // Looks up for the pattern @Value("${my.property:123}"), works also if the default value is absent.
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+?)(?::(.+?))?}");

    // Looks up for the pattern ".getProperty()", such as "#{environment.getProperty('server.port')}"
    // Note that as of now we're not checking that the actual receiver is instanceof Spring's Environment
    private static final Pattern ENVIRONMENT_GET_PROPERTY_CALLS_PATTERN =
            Pattern.compile("getProperty\\s*\\(\\s*['\"]([^'\"]+)['\"]");

    // Looks up for the pattern that uses property source name indexed usage, such as
    // "#{systemProperties['server.port']}"
    private final Pattern SYSTEM_PROPERTIES_ACCESS_PATTERN = Pattern.compile("\\[\\s*['\"]([^'\"]+)['\"]\\s*]");

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
        return MergedAnnotations.from(element)
                .get(Value.class)
                .synthesize(MergedAnnotation::isPresent)
                .orElse(null);
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
        Matcher matcher = ENVIRONMENT_GET_PROPERTY_CALLS_PATTERN.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }

        matcher = SYSTEM_PROPERTIES_ACCESS_PATTERN.matcher(expression);
        while (matcher.find()) {
            propertyNames.add(matcher.group(1));
        }
    }
}
