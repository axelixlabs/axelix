package com.nucleonforge.axile.spring.beans;

import com.nucleonforge.axile.common.api.BeansFeed;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link BeanEnricher}.
 *
 * @since 04.07.2025
 * @author Nikita Kirillov
 */
public class DefaultBeanEnricher implements BeanEnricher {

    private final DefaultQualifiersRegistry qualifiersRegistry;

    public DefaultBeanEnricher() {
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
    }

    @Override
    public Optional<BeansFeed.Bean> enrich(String beanName, BeansEndpoint.BeanDescriptor beanDescriptor, ApplicationContext context) {
        if (!(context instanceof ConfigurableApplicationContext configurableContext)) {
            return Optional.empty();
        }

        ConfigurableListableBeanFactory beanFactory = configurableContext.getBeanFactory();

        if (!beanFactory.containsBeanDefinition(beanName)) {
            return Optional.empty();
        }

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

        return Optional.of(new BeansFeed.Bean(
            beanDescriptor.getScope(),
            beanDescriptor.getType().getName(),
            analyzeProxyType(beanName, beanFactory),
            Arrays.stream(beanDescriptor.getAliases()).collect(Collectors.toSet()),
            Arrays.stream(beanDescriptor.getDependencies()).collect(Collectors.toSet()),
            beanDefinition.isLazyInit(),
            beanDefinition.isPrimary(),
            qualifiersRegistry.getQualifiers(beanName),
            analyzeBeanSource(beanDefinition, beanName, beanFactory)
        ));
    }

    private BeansFeed.ProxyType analyzeProxyType(String beanName, ConfigurableListableBeanFactory beanFactory) {
            Class<?> beanType = beanFactory.getType(beanName);
            if (AopUtils.isCglibProxy(beanType)) {
               return BeansFeed.ProxyType.JDK_PROXY;
            } else if (AopUtils.isCglibProxy(beanType)) {
               return BeansFeed.ProxyType.CGLIB;
            }
       return BeansFeed.ProxyType.NO_PROXYING;
    }

    private BeansFeed.BeanSource analyzeBeanSource(BeanDefinition beanDefinition, String beanName,
                                                   ConfigurableListableBeanFactory beanFactory) {
        if (beanDefinition.getFactoryBeanName() != null && beanDefinition.getFactoryMethodName() != null) {
            return new BeansFeed.BeanMethod(beanDefinition.getFactoryBeanName(), beanDefinition.getFactoryMethodName());
        }

        if (beanDefinition.getFactoryMethodName() != null) {
            String enclosingClassName = extractEnclosingClassName(beanDefinition, beanName, beanFactory);
            return new BeansFeed.BeanMethod(Objects.requireNonNull(enclosingClassName), beanDefinition.getFactoryMethodName());
        }

        if (beanDefinition.getBeanClassName() != null && isFactoryBeanClass(beanDefinition.getBeanClassName())) {
            return new BeansFeed.FactoryBean(beanDefinition.getBeanClassName());
        }

        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedDef) {
            AnnotationMetadata metadata = annotatedDef.getMetadata();
            if (metadata.hasAnnotation(Component.class.getName()) ||
                metadata.hasAnnotation(Service.class.getName()) ||
                metadata.hasAnnotation(Repository.class.getName()) ||
                metadata.hasAnnotation(Controller.class.getName()) ||
                metadata.hasAnnotation(Configuration.class.getName())) {
                return new BeansFeed.ComponentVariant();
            }
        }

        return new BeansFeed.UnknownBean();
    }

    @Nullable
    private String extractEnclosingClassName(BeanDefinition beanDefinition, String beanName,
                                             ConfigurableListableBeanFactory beanFactory) {
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

    private boolean isFactoryBeanClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return FactoryBean.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
