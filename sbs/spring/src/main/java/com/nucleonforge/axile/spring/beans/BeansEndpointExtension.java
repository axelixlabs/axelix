package com.nucleonforge.axile.spring.beans;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

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
    private final BeanAnalyzer analyzer;

    public BeansEndpointExtension(BeansEndpoint delegate, BeanAnalyzer analyzer) {
        this.delegate = delegate;
        this.analyzer = analyzer;
    }

    @ReadOperation
    public WebEndpointResponse<Map<String, Object>> beans() {
        BeansEndpoint.BeansDescriptor descriptor = delegate.beans();

        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> contexts = new HashMap<>();

        descriptor.getContexts().forEach((contextId, contextDescriptor) -> {
            Map<String, Object> beans = new HashMap<>();

            contextDescriptor.getBeans().forEach((beanName, beanDescriptor) -> {
                Map<String, Object> beanInfo = new HashMap<>();
                String type = beanDescriptor.getType() != null
                        ? beanDescriptor.getType().getName()
                        : null;

                beanInfo.put("aliases", beanDescriptor.getAliases());
                beanInfo.put("scope", beanDescriptor.getScope());
                beanInfo.put("type", type);
                beanInfo.put("resource", beanDescriptor.getResource());
                beanInfo.put("dependencies", beanDescriptor.getDependencies());

                analyzer.analyze(beanName).ifPresent(profile -> {
                    beanInfo.put("isLazyInit", profile.isLazyInit());
                    beanInfo.put("isPrimary", profile.isPrimary());
                    beanInfo.put("qualifiers", profile.qualifiers());
                    beanInfo.put("enclosingClassName", profile.enclosingClassName());
                    beanInfo.put("methodName", profile.methodName());
                    beanInfo.put("factoryBeanName", profile.factoryBeanName());
                });

                beans.put(beanName, beanInfo);
            });

            Map<String, Object> contextInfo = new HashMap<>();
            contextInfo.put("beans", beans);
            contextInfo.put("parentId", contextDescriptor.getParentId());

            contexts.put(contextId, contextInfo);
        });

        responseBody.put("contexts", contexts);

        return new WebEndpointResponse<>(responseBody);
    }
}
