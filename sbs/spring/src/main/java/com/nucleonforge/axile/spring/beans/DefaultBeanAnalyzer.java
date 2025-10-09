package com.nucleonforge.axile.spring.beans;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

/**
 * Default implementation of {@link BeanAnalyzer} that inspects bean definitions
 * in a Spring application context.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public class DefaultBeanAnalyzer implements BeanAnalyzer {

    private final ConfigurableListableBeanFactory beanFactory;

    private final DefaultQualifiersRegistry qualifiersRegistry;

    public DefaultBeanAnalyzer(ApplicationContext context) {
        this.beanFactory = (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory();
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
    }

    @Override
    public Optional<BeanProfile> analyze(String beanName) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return Optional.empty();
        }

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

        return Optional.of(new BeanProfile(
                beanDefinition.isLazyInit(),
                beanDefinition.isPrimary(),
                qualifiersRegistry.getQualifiers(beanName),
                extractEnclosingClassName(beanDefinition, beanName),
                extractMethodName(beanDefinition),
                extractFactoryBeanName(beanDefinition)));
    }

    @Nullable
    private String extractEnclosingClassName(BeanDefinition beanDefinition, String beanName) {
        if (beanDefinition.getFactoryBeanName() != null) {
            return beanDefinition.getFactoryBeanName();
        }

        if (beanDefinition.getSource() instanceof StandardMethodMetadata metadata) {
            return metadata.getDeclaringClassName();
        }

        if (beanDefinition.getBeanClassName() != null) {
            return beanDefinition.getBeanClassName();
        }

        try {
            Class<?> beanType = beanFactory.getType(beanName);
            return beanType != null ? beanType.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private String extractMethodName(BeanDefinition def) {
        if (def.getFactoryMethodName() != null) {
            return def.getFactoryMethodName();
        }
        return null;
    }

    @Nullable
    private String extractFactoryBeanName(BeanDefinition beanDefinition) {
        if (beanDefinition.getFactoryBeanName() != null && beanDefinition.getFactoryMethodName() != null) {
            return beanDefinition.getFactoryBeanName();
        }

        if (beanDefinition.getBeanClassName() != null && isFactoryBeanClass(beanDefinition.getBeanClassName())) {
            return beanDefinition.getBeanClassName();
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
}
