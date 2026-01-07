/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axelix.sbs.spring.beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.util.ClassUtils;

import com.nucleonforge.axelix.common.api.BeansFeed;

/**
 * Class that is capable to assemble the {@link com.nucleonforge.axelix.common.api.BeansFeed}.
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
        BeansEndpoint.BeansDescriptor actuatorResponse = delegate.beans();

        Map<String, BeansFeed.Context> contexts = new HashMap<>();

        actuatorResponse.getContexts().forEach((contextId, contextDescriptor) -> {
            Map<String, BeansFeed.Bean> beans = new HashMap<>();
            ConfigurableApplicationContext targetContext = findConfigurableContextForBean(contextId);

            if (targetContext != null) {

                // TODO: Consider to cache the configprops beans extracted from the context later on
                Map<String, ConfigurationPropertiesBean> configPropsBeanMap =
                        ConfigurationPropertiesBean.getAll(targetContext);

                contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                    BeanMetaInfo metaInfo = enricher.extract(beanName, targetContext.getBeanFactory());

                    Set<BeansFeed.BeanDependency> enrichedDependencies = resolveDependencies(
                            beanDescriptor.getDependencies(), configPropsBeanMap, metaInfo.beanSource());

                    String beanType = resolveBeanTypeName(beanDescriptor, metaInfo.beanSource());

                    beans.put(
                            beanName,
                            new BeansFeed.Bean(
                                    beanDescriptor.getScope(),
                                    beanType,
                                    metaInfo.proxyType(),
                                    toSet(beanDescriptor.getAliases()),
                                    metaInfo.autoConfigurationRef(),
                                    enrichedDependencies,
                                    metaInfo.isLazyInit(),
                                    metaInfo.isPrimary(),
                                    configPropsBeanMap.containsKey(beanName),
                                    metaInfo.qualifiers(),
                                    metaInfo.beanSource()));
                });
            }

            contexts.put(contextId, new BeansFeed.Context(contextDescriptor.getParentId(), beans));
        });

        return new BeansFeed(contexts);
    }

    private static Set<String> toSet(String... strings) {
        return Arrays.stream(strings).collect(Collectors.toSet());
    }

    @Nullable
    private ConfigurableApplicationContext findConfigurableContextForBean(String contextId) {
        ApplicationContext current = context;

        while (current != null) {

            if (contextId.equals(current.getId()) && current instanceof ConfigurableApplicationContext cac) {
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
                    if (beanSource instanceof BeansFeed.BeanMethod beanMethod) {
                        try {
                            String[] beanNamesForType = context.getBeanNamesForType(Class.forName(beanMethod.enclosingClassFullName()));
                            return !Arrays.asList(beanNamesForType).contains(dep);
                        } catch (ClassNotFoundException e) {
                            // TODO: Refactor this later
                            return true;
                        }
                    } else {
                        return true;
                    }
                })
                .map(depName -> new BeansFeed.BeanDependency(depName, configPropsBeanMap.containsKey(depName)))
                .collect(Collectors.toSet());
    }

    private String resolveBeanTypeName(BeansEndpoint.BeanDescriptor beanDescriptor, BeansFeed.BeanSource beanSource) {
        Class<?> clazz = beanDescriptor.getType();

        if (clazz.isHidden() && beanSource instanceof BeansFeed.BeanMethod && clazz.getInterfaces().length > 0) {
            return clazz.getInterfaces()[0].getName();
        }

        return ClassUtils.getUserClass(clazz).getName();
    }
}
