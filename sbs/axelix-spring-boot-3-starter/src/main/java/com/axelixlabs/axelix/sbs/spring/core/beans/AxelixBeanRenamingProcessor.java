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
import org.springframework.util.Assert;
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
 * @author Mikhail Polivakha
 */
public class AxelixBeanRenamingProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * Common packages for Axelix classes.
     */
    private static final String TARGET_PACKAGE = "com.axelixlabs.axelix";

    public static final String PREFIX = "axelix";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames().clone();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition instanceof AbstractBeanDefinition) {
                potentiallyRenameBeanDefinition(registry, beanName, (AbstractBeanDefinition) beanDefinition);
            }
        }
    }

    private void potentiallyRenameBeanDefinition(
            BeanDefinitionRegistry registry, String beanName, AbstractBeanDefinition beanDefinition) {

        if (isBeanCreatedViaBeanAnnotation(registry, beanDefinition)) {
            Assert.notNull(
                    beanDefinition.getFactoryBeanName(), "Enclosing class of the bean cannot be null at this point");

            BeanDefinition factoryBd = registry.getBeanDefinition(beanDefinition.getFactoryBeanName());
            String factoryClassName = factoryBd.getBeanClassName();

            if (isAxelixBean(factoryClassName)) {
                prependPostfixIfNotPresent(registry, beanName, beanDefinition);
            }
        }
    }

    private static void prependPostfixIfNotPresent(
            BeanDefinitionRegistry registry, String beanName, AbstractBeanDefinition beanDefinition) {
        if (!beanName.toLowerCase().startsWith(PREFIX)) {
            String newBeanName = PREFIX + StringUtils.capitalize(beanName);
            registry.removeBeanDefinition(beanName);
            registry.registerBeanDefinition(newBeanName, beanDefinition);
        }
    }

    private boolean isBeanCreatedViaBeanAnnotation(BeanDefinitionRegistry registry, AbstractBeanDefinition abd) {
        return abd.getFactoryMethodName() != null
                && abd.getFactoryBeanName() != null
                && registry.containsBeanDefinition(abd.getFactoryBeanName());
    }

    private boolean isAxelixBean(@Nullable String factoryClassName) {
        return factoryClassName != null && factoryClassName.startsWith(TARGET_PACKAGE);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
