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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.type.MethodMetadata;
import org.springframework.scheduling.annotation.SchedulingConfiguration;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

/**
 * Replaces the {@code ScheduledAnnotationBeanPostProcessor} registered by {@code @EnableScheduling} with the
 * {@link AxelixScheduledAnnotationBeanPostProcessor}. The processor declared by the user is left as is.
 *
 * @author Nikita Kirillov
 */
public class AxelixScheduledAnnotationProcessorRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AxelixScheduledAnnotationProcessorRegistrar.class);

    private static final String PROCESSOR_BEAN_NAME =
            TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(PROCESSOR_BEAN_NAME)) {
            return;
        }
        if (!isRegisteredByEnableScheduling(registry.getBeanDefinition(PROCESSOR_BEAN_NAME))) {
            log.info("The @Scheduled annotation processor is not registered by @EnableScheduling, so it is left as is. "
                    + "The executions of the @Scheduled tasks will not be recorded, and the tasks of the "
                    + "repeatable @Scheduled annotations of the same method will not be told apart");
            return;
        }

        RootBeanDefinition processor = new RootBeanDefinition(AxelixScheduledAnnotationBeanPostProcessor.class);
        processor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        registry.removeBeanDefinition(PROCESSOR_BEAN_NAME);
        registry.registerBeanDefinition(PROCESSOR_BEAN_NAME, processor);
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) {}

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static boolean isRegisteredByEnableScheduling(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            MethodMetadata factoryMethod = annotatedBeanDefinition.getFactoryMethodMetadata();
            return factoryMethod != null
                    && SchedulingConfiguration.class.getName().equals(factoryMethod.getDeclaringClassName());
        }
        return false;
    }
}
