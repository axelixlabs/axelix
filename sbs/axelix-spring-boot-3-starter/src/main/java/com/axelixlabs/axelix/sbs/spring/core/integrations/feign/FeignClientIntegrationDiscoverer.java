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
package com.axelixlabs.axelix.sbs.spring.core.integrations.feign;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.integration.FeignIntegration;
import com.axelixlabs.axelix.common.domain.http.HttpVersion;
import com.axelixlabs.axelix.sbs.spring.core.integrations.IntegrationComponentDiscoverer;

/**
 * Discovers HTTP service integrations based on {@link FeignClient} annotations
 * in the Spring application context.
 *
 * <p>This component scans all beans annotated with {@link FeignClient} and collects
 * metadata about the services they connect to, transforming them into
 * {@link FeignIntegration} objects.</p>
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class FeignClientIntegrationDiscoverer implements IntegrationComponentDiscoverer<FeignIntegration> {

    private static final String UNKNOWN = "UNKNOWN";

    private final ApplicationContext context;

    private final DiscoveryClient discoveryClient;

    public FeignClientIntegrationDiscoverer(ApplicationContext context, DiscoveryClient discoveryClient) {
        this.context = context;
        this.discoveryClient = discoveryClient;
    }

    public Set<FeignIntegration> discoverIntegrations() {
        return Arrays.stream(context.getBeanNamesForAnnotation(FeignClient.class))
                .map(this::extractFeignClientClass)
                .filter(Objects::nonNull)
                .map(this::createIntegration)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private @Nullable FeignIntegration createIntegration(Class<?> feignType) {

        FeignClient feignClient = AnnotatedElementUtils.findMergedAnnotation(feignType, FeignClient.class);

        if (feignClient == null) {
            return null;
        }

        String serviceName = extractServiceName(feignClient);

        List<String> networkAddresses = extractNetworkAddresses(feignClient, serviceName);

        List<FeignIntegration.FeignHttpMethod> httpMethods = Arrays.stream(feignType.getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .map(method -> createHttpMethod(method, feignClient.path()))
                .filter(Objects::nonNull)
                .toList();

        return new FeignIntegration(serviceName, networkAddresses, HttpVersion.V1_1.getDisplay(), httpMethods);
    }

    private FeignIntegration.@Nullable FeignHttpMethod createHttpMethod(Method method, String feignPath) {

        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        if (mapping == null) {
            return null;
        }

        String httpMethod = mapping.method().length == 0 ? UNKNOWN : mapping.method()[0].name();

        return new FeignIntegration.FeignHttpMethod(httpMethod, pickPath(mapping.path(), feignPath));
    }

    private @Nullable Class<?> extractFeignClientClass(String beanName) {

        Object bean = context.getBean(beanName);
        Class<?> beanClass = bean.getClass();

        if (AnnotatedElementUtils.hasAnnotation(beanClass, FeignClient.class)) {
            return beanClass;
        }

        for (Class<?> interfaces : beanClass.getInterfaces()) {
            if (AnnotatedElementUtils.hasAnnotation(interfaces, FeignClient.class)) {
                return interfaces;
            }
        }

        return null;
    }

    private String extractServiceName(FeignClient feignClient) {
        if (!feignClient.name().isBlank()) {
            return feignClient.name();
        }

        return feignClient.value();
    }

    private List<String> extractNetworkAddresses(FeignClient feignClient, String serviceId) {
        if (!feignClient.url().isBlank()) {
            return List.of(feignClient.url());
        }

        // Retrieving the network address from the DiscoveryClient can be significantly delayed,
        // as it depends on the service registration mechanism, which is outside our control.
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        return instances.stream()
                .map(instance -> instance.getUri().toString())
                .filter(service -> !service.isBlank())
                .toList();
    }

    private @Nullable String pickPath(String[] path, String feignPath) {
        if (path.length > 0) {
            return feignPath + path[0];
        }

        return !feignPath.isBlank() ? feignPath : null;
    }
}
