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
package com.axelixlabs.axelix.sbs.spring.beans;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

/**
 * The {@link BeanFactoryPostProcessor} that constructs the internal {@link DefaultQualifiersRegistry}.
 *
 * @author Mikhail Polivakha
 */
public class QualifiersPersistencePostProcessor implements BeanFactoryPostProcessor, Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {

                MethodMetadata factoryMethodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();

                if (factoryMethodMetadata != null) {
                    MergedAnnotations factoryBeanMetadata = factoryMethodMetadata.getAnnotations();
                    registerForBean(beanName, factoryBeanMetadata);
                }

                AnnotationMetadata declarationMetadata = annotatedBeanDefinition.getMetadata();
                registerForBean(beanName, declarationMetadata.getAnnotations());
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void registerForBean(String beanName, MergedAnnotations annotations) {
        List<String> qualifiers = annotations.stream()
                .filter(it -> Qualifier.class.equals(it.getType()))
                .map(it -> it.getValue(MergedAnnotation.VALUE, String.class).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        DefaultQualifiersRegistry.INSTANCE.registerQualifiers(beanName, qualifiers);
    }
}
