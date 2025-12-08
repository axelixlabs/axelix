/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.beans;

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
