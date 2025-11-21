package com.nucleonforge.axile.sbs.spring.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
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
 * @since 04.07.2025
 */
@NullMarked
public class DefaultBeanMetaInfoExtractor implements BeanMetaInfoExtractor {

    private final DefaultQualifiersRegistry qualifiersRegistry;
    private final ConfigurableListableBeanFactory beanFactory;

    public DefaultBeanMetaInfoExtractor(ConfigurableListableBeanFactory configurableBeanFactory) {
        this.beanFactory = configurableBeanFactory;
        this.qualifiersRegistry = DefaultQualifiersRegistry.INSTANCE;
    }

    @Override
    public BeanMetaInfo extract(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        Object bean = beanFactory.getBean(beanName);

        return new BeanMetaInfo(
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
}
