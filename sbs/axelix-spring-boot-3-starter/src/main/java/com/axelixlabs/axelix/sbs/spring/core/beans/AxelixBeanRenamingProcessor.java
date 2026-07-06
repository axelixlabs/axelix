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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.StringUtils;

/**
 * A {@link BeanDefinitionRegistryPostProcessor} responsible for automatically renaming Spring beans
 * declared via {@code @Bean} methods within the Axelix starter infrastructure.
 * <p>
 * The processor identifies target beans by verifying two main criteria:
 * <ul>
 *     <li>The bean must have a non-null {@code factoryMethodName} (indicating it was declared using a {@code @Bean} annotation).</li>
 *     <li>The declaring configuration class, resolved via {@code factoryBeanName}, must reside within the designated Axelix package namespace.</li>
 * </ul>
 * <p>
 * The renaming strategy applies a designated prefix (e.g., {@code "axelix"}) to the bean name.
 * But if the bean name already starts with the 'axelix' prefix, the processor ignores it.
 *
 * @author Vyacheslav Yanin
 */
public class AxelixBeanRenamingProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * Common packages for each sbs.
     */
    private static final String TARGET_PACKAGE = "com.axelixlabs.axelix.sbs.spring.";

    private static final String PREFIX = "axelix";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames().clone();
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("entitymanager")) {
                beanNames = beanNames.clone();
            }

            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
                if (isBeanCreatedViaBeanAnnotation(abstractBeanDefinition)) {
                    String factoryBeanName = abstractBeanDefinition.getFactoryBeanName();

                    if (isFactoryBeanRegistered(registry, factoryBeanName)) {
                        BeanDefinition factoryBd = registry.getBeanDefinition(factoryBeanName);
                        String factoryClassName = factoryBd.getBeanClassName();

                        if (isAxelixBean(factoryClassName)) {
                            if (beanName.startsWith(PREFIX)) {
                                continue;
                            }
                            String newBeanName = PREFIX + StringUtils.capitalize(beanName);
                            replaceBeanDefinition(registry, beanName, newBeanName, abstractBeanDefinition);
                        }
                    }
                }
            }
        }
    }

    private boolean isBeanCreatedViaBeanAnnotation(AbstractBeanDefinition abd) {
        return abd.getFactoryMethodName() != null;
    }

    private boolean isFactoryBeanRegistered(BeanDefinitionRegistry registry, @Nullable String factoryBeanName) {
        return factoryBeanName != null && registry.containsBeanDefinition(factoryBeanName);
    }

    private boolean isAxelixBean(@Nullable String factoryClassName) {
        return factoryClassName != null && factoryClassName.startsWith(TARGET_PACKAGE);
    }

    private void replaceBeanDefinition(
            BeanDefinitionRegistry registry,
            String beanName,
            String newBeanName,
            AbstractBeanDefinition abstractBeanDefinition) {
        registry.removeBeanDefinition(beanName);
        registry.registerBeanDefinition(newBeanName, abstractBeanDefinition);
        registry.registerAlias(newBeanName, beanName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
