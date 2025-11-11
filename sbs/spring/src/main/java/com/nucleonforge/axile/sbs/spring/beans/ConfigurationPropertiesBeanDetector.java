package com.nucleonforge.axile.sbs.spring.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class for detecting Spring ConfigurationProperties beans in the application context.
 *
 * @since 10.11.2025
 * @author Nikita Kirillov
 */
public class ConfigurationPropertiesBeanDetector {

    private ConfigurationPropertiesBeanDetector() {}

    public static boolean isConfigurationPropertiesBean(ConfigurableListableBeanFactory beanFactory, String beanName) {
        try {
            if (beanFactory.getBeanDefinition(beanName).isAbstract()) {
                return false;
            }
            if (beanFactory.findAnnotationOnBean(beanName, ConfigurationProperties.class) != null) {
                return true;
            }
            Method factoryMethod = findFactoryMethod(beanFactory, beanName);
            if (factoryMethod != null) {
                return findMergedAnnotation(factoryMethod).isPresent();
            }
            return false;
        } catch (NoSuchBeanDefinitionException ex) {
            return false;
        }
    }

    @Nullable
    private static Method findFactoryMethod(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (beanFactory.containsBeanDefinition(beanName)) {
            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
                Method resolvedFactoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
                if (resolvedFactoryMethod != null) {
                    return resolvedFactoryMethod;
                }
            }
            return findFactoryMethodUsingReflection(beanFactory, beanDefinition);
        }
        return null;
    }

    @Nullable
    private static Method findFactoryMethodUsingReflection(
            ConfigurableListableBeanFactory beanFactory, BeanDefinition beanDefinition) {
        String factoryMethodName = beanDefinition.getFactoryMethodName();
        String factoryBeanName = beanDefinition.getFactoryBeanName();

        if (factoryMethodName == null || factoryBeanName == null) {
            return null;
        }
        Class<?> factoryType = beanFactory.getType(factoryBeanName);
        if (factoryType != null && factoryType.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
            factoryType = factoryType.getSuperclass();
            AtomicReference<Method> factoryMethod = new AtomicReference<>();
            ReflectionUtils.doWithMethods(factoryType, (method) -> {
                if (method.getName().equals(factoryMethodName)) {
                    factoryMethod.set(method);
                }
            });
            return factoryMethod.get();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> MergedAnnotation<A> findMergedAnnotation(AnnotatedElement element) {
        return MergedAnnotations.from(element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get((Class<A>) ConfigurationProperties.class);
    }
}
