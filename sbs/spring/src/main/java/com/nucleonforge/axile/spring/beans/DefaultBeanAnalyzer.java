package com.nucleonforge.axile.spring.beans;

import java.lang.reflect.Method;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

/**
 * Default implementation of {@link BeanAnalyzer} that inspects bean definitions
 * in a Spring application context.
 * <p>
 * This analyzer provides metadata such as actual class of the bean, method that defined the bean,
 * scope, factory method.
 * <p>
 * Internally uses {@link ConfigurableListableBeanFactory} to access {@link BeanDefinition}
 * and extract relevant details.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public class DefaultBeanAnalyzer implements BeanAnalyzer {

    private final ConfigurableListableBeanFactory beanFactory;

    public DefaultBeanAnalyzer(ApplicationContext context) {
        this.beanFactory = (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory();
    }

    @Override
    public Optional<BeanProfile> analyze(String beanName) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return Optional.empty();
        }

        BeanDefinition def = beanFactory.getBeanDefinition(beanName);

        return Optional.of(new BeanProfile(
                beanName,
                resolveBeanClass(def, beanName),
                resolveDefiningMethod(def),
                extractScope(def),
                isCreatedByFactoryBean(def)));
    }

    @SuppressWarnings("NullAway") // Technically, the class name of a bean cannot be null at runtime.
    private Class<?> resolveBeanClass(BeanDefinition def, String beanName) {
        if (def instanceof AbstractBeanDefinition abd && abd.getBeanClassName() != null) {
            try {
                return Class.forName(abd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (def.getSource() instanceof StandardMethodMetadata metadata) {
            try {
                return Class.forName(metadata.getDeclaringClassName());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        try {
            return beanFactory.getType(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Nullable
    private Method resolveDefiningMethod(BeanDefinition def) {
        if (def.getSource() instanceof StandardMethodMetadata metadata) {
            return metadata.getIntrospectedMethod();
        }
        String factoryMethodName = def.getFactoryMethodName();
        String factoryBeanName = def.getFactoryBeanName();

        if (factoryMethodName != null && factoryBeanName != null) {
            Object factoryBean = beanFactory.getBean(factoryBeanName);
            Class<?> factoryClass = AopUtils.getTargetClass(factoryBean);

            for (Method method : factoryClass.getDeclaredMethods()) {
                if (method.getName().equals(factoryMethodName)) {
                    return method;
                }
            }
        }

        return null;
    }

    private String extractScope(BeanDefinition def) {
        String scope = def.getScope();
        return (scope == null || scope.isBlank()) ? "singleton" : scope;
    }

    private boolean isCreatedByFactoryBean(BeanDefinition def) {
        return def.getFactoryBeanName() != null;
    }
}
