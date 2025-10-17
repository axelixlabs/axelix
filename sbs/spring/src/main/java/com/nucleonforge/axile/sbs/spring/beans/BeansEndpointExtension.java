package com.nucleonforge.axile.sbs.spring.beans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.Bean;
import com.nucleonforge.axile.common.api.BeansFeed.Context;

/**
 * Web extension for Spring Boot Beans Actuator endpoint.
 * Extends standard beans response with additional bean metadata.
 *
 * @since 08.10.2025
 * @author Nikita Kirillov
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

            contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                ConfigurableApplicationContext targetContext = findConfigurableContextForBean(contextId);

                if (targetContext != null) {
                    BeanMetaInfo metaInfo = enricher.extract(beanName, targetContext.getBeanFactory());
                    beans.put(
                            beanName,
                            new Bean(
                                    beanDescriptor.getScope(),
                                    ClassUtils.getUserClass(beanDescriptor.getType())
                                            .getName(),
                                    metaInfo.proxyType(),
                                    toSet(beanDescriptor.getAliases()),
                                    toSet(beanDescriptor.getDependencies()),
                                    metaInfo.isLazyInit(),
                                    metaInfo.isPrimary(),
                                    metaInfo.qualifiers(),
                                    metaInfo.beanSource()));
                }
            });

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
}
