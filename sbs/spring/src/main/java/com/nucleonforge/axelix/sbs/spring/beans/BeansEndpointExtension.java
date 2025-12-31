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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint.BeanDescriptor;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

import com.nucleonforge.axelix.common.api.BeansFeed;
import com.nucleonforge.axelix.common.api.BeansFeed.Bean;
import com.nucleonforge.axelix.common.api.BeansFeed.BeanDependency;
import com.nucleonforge.axelix.common.api.BeansFeed.BeanMethod;
import com.nucleonforge.axelix.common.api.BeansFeed.BeanSource;
import com.nucleonforge.axelix.common.api.BeansFeed.Context;

/**
 * Web extension for Spring Boot Beans Actuator endpoint.
 * Extends standard beans response with additional bean metadata.
 *
 * @since 08.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@EndpointWebExtension(endpoint = BeansEndpoint.class)
public class BeansEndpointExtension {

    private final BeansEndpoint delegate;
    private final BeanMetaInfoExtractor enricher;
    private final ConfigurableApplicationContext context;

    public BeansEndpointExtension(
            BeansEndpoint delegate, BeanMetaInfoExtractor enricher, ConfigurableApplicationContext context) {
        this.delegate = delegate;
        this.enricher = enricher;
        this.context = context;
    }

    @ReadOperation
    public WebEndpointResponse<BeansFeed> beans() {
        BeansEndpoint.BeansDescriptor actuatorResponse = delegate.beans();

        Map<String, Context> contexts = new HashMap<>();

        actuatorResponse.getContexts().forEach((contextId, contextDescriptor) -> {
            Map<String, Bean> beans = new HashMap<>();
            ConfigurableApplicationContext targetContext = findConfigurableContextForBean(contextId);

            if (targetContext != null) {

                // TODO: Consider to cache the configprops beans extracted from the context later on
                Map<String, ConfigurationPropertiesBean> configPropsBeanMap =
                        ConfigurationPropertiesBean.getAll(targetContext);

                contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                    BeanMetaInfo metaInfo = enricher.extract(beanName, targetContext.getBeanFactory());

                    Set<BeanDependency> enrichedDependencies =
                            enrichDependencies(beanDescriptor.getDependencies(), configPropsBeanMap);

                    String beanType = resolveBeanTypeName(beanDescriptor, metaInfo.beanSource());

                    beans.put(
                            beanName,
                            new Bean(
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

            contexts.put(contextId, new Context(contextDescriptor.getParentId(), beans));
        });

        return new WebEndpointResponse<>(new BeansFeed(contexts));
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

    private Set<BeanDependency> enrichDependencies(
            String[] dependencies, Map<String, ConfigurationPropertiesBean> configPropsBeanMap) {
        if (dependencies == null || dependencies.length == 0) {
            return Collections.emptySet();
        }

        return Arrays.stream(dependencies)
                .filter(Objects::nonNull)
                .filter(dep -> !dep.trim().isEmpty())
                .map(depName -> new BeanDependency(depName, configPropsBeanMap.containsKey(depName)))
                .collect(Collectors.toSet());
    }

    private String resolveBeanTypeName(BeanDescriptor beanDescriptor, BeanSource beanSource) {
        Class<?> clazz = beanDescriptor.getType();

        if (clazz.isHidden() && beanSource instanceof BeanMethod && clazz.getInterfaces().length > 0) {
            return clazz.getInterfaces()[0].getName();
        }

        return ClassUtils.getUserClass(clazz).getName();
    }
}
