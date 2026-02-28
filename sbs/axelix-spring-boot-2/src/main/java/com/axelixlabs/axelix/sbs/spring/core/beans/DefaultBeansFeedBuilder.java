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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.sbs.spring.core.utils.BeanNameUtils;
import com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils;

/**
 * Class that is capable to assemble the {@link BeansFeed}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultBeansFeedBuilder implements BeansFeedBuilder {

    private final BeansEndpoint delegate;
    private final BeanMetaInfoExtractor enricher;
    private final ConfigurableApplicationContext context;

    public DefaultBeansFeedBuilder(BeanMetaInfoExtractor enricher, ConfigurableApplicationContext context) {
        // TODO: replace the actuator call with dedicated, self-written API
        this.delegate = new BeansEndpoint(context);
        this.enricher = enricher;
        this.context = context;
    }

    @Override
    @NonNull
    public BeansFeed buildBeansFeed() {
        BeansEndpoint.ApplicationBeans actuatorResponse = delegate.beans();
        List<BeansFeed.Bean> beans = new ArrayList<>();

        actuatorResponse.getContexts().forEach((contextId, contextDescriptor) -> {
            ConfigurableApplicationContext targetContext = findConfigurableContextForBean(contextId);

            if (targetContext != null) {

                // TODO: Consider to cache the configprops beans extracted from the context later on
                Map<String, ConfigurationPropertiesBean> configPropsBeanMap =
                        ConfigurationPropertiesBean.getAll(targetContext);

                contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                    BeanMetaInfo metaInfo = enricher.extract(beanName, targetContext.getBeanFactory());

                    Set<BeansFeed.BeanDependency> enrichedDependencies = resolveDependencies(
                            beanDescriptor.getDependencies(), configPropsBeanMap, metaInfo.getBeanSource());

                    Class<?> clazz = beanDescriptor.getType();
                    String beanType =
                            BeanNameUtils.resolveBeanTypeName(clazz, metaInfo.getBeanSource(), clazz.isSynthetic());

                    boolean isConfigPropsBean = configPropsBeanMap.containsKey(beanName);

                    beans.add(new BeansFeed.Bean(
                            BeanNameUtils.withoutConfigPropsPrefix(beanName, isConfigPropsBean),
                            beanType,
                            beanDescriptor.getScope(),
                            metaInfo.getProxyType(),
                            StringUtils.toSet(beanDescriptor.getAliases()),
                            metaInfo.getAutoConfigurationRef(),
                            enrichedDependencies,
                            metaInfo.isPrimary(),
                            metaInfo.isLazyInit(),
                            isConfigPropsBean,
                            metaInfo.getQualifiers(),
                            metaInfo.getBeanSource()));
                });
            }
        });

        return new BeansFeed(beans);
    }

    @Nullable
    private ConfigurableApplicationContext findConfigurableContextForBean(String contextId) {
        ApplicationContext current = context;

        while (current != null) {

            if (contextId.equals(current.getId()) && current instanceof ConfigurableApplicationContext) {
                ConfigurableApplicationContext cac = (ConfigurableApplicationContext) current;
                return cac;
            }

            current = current.getParent();
        }

        return null;
    }

    private Set<BeansFeed.BeanDependency> resolveDependencies(
            String[] dependencies,
            Map<String, ConfigurationPropertiesBean> configPropsBeanMap,
            BeansFeed.BeanSource beanSource) {

        if (dependencies == null || dependencies.length == 0) {
            return Collections.emptySet();
        }

        return Arrays.stream(dependencies)
                .filter(Objects::nonNull)
                .filter(dep -> !dep.trim().isEmpty())
                .filter(dep -> {
                    // For some reason, @Bean methods inside configuration classes have enclosing
                    // @Configuration class as their dependency.
                    if (beanSource instanceof BeansFeed.BeanMethod) {
                        BeansFeed.BeanMethod beanMethod = (BeansFeed.BeanMethod) beanSource;
                        try {
                            String[] beanNamesForType =
                                    context.getBeanNamesForType(Class.forName(beanMethod.getEnclosingClassFullName()));
                            return !Arrays.asList(beanNamesForType).contains(dep);
                        } catch (ClassNotFoundException e) {
                            // TODO: Refactor this later
                            return true;
                        }
                    } else {
                        return true;
                    }
                })
                .map(depName -> {
                    boolean isConfigPropsBean = configPropsBeanMap.containsKey(depName);
                    return new BeansFeed.BeanDependency(
                            BeanNameUtils.withoutConfigPropsPrefix(depName, isConfigPropsBean), isConfigPropsBean);
                })
                .collect(Collectors.toSet());
    }
}
