package com.nucleonforge.axile.spring.beans;

import java.util.HashMap;
import java.util.Map;

import com.nucleonforge.axile.common.api.BeansFeed;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.context.ApplicationContext;

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
    private final BeanEnricher enricher;
    private final ApplicationContext context;

    public BeansEndpointExtension(BeansEndpoint delegate, BeanEnricher enricher, ApplicationContext context) {
        this.delegate = delegate;
        this.enricher = enricher;
        this.context = context;
    }

    @ReadOperation
    public WebEndpointResponse<BeansFeed> beans() {
        BeansEndpoint.BeansDescriptor descriptor = delegate.beans();

        Map<String, BeansFeed.Context> contexts = new HashMap<>();

        descriptor.getContexts().forEach((contextId, contextDescriptor) -> {
            Map<String, BeansFeed.Bean> beans = new HashMap<>();

            contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                ApplicationContext targetContext = findContextForBean(contextId);
                if (targetContext != null) {
                    enricher.enrich(beanName, beanDescriptor, targetContext)
                        .ifPresent(bean -> beans.put(beanName, bean));
                }
            });

            contexts.put(contextId, new BeansFeed.Context(
                contextDescriptor.getParentId(),
                beans
            ));
        });

        return new WebEndpointResponse<>(new BeansFeed(contexts));
    }

    @Nullable
    private ApplicationContext findContextForBean(String contextId) {
        ApplicationContext current = context;
        while (current != null) {
            if (contextId.equals(current.getId())) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
