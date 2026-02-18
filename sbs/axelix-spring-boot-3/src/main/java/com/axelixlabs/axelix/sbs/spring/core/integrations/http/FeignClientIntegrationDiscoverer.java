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
package com.axelixlabs.axelix.sbs.spring.core.integrations.http;

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

import com.axelixlabs.axelix.common.api.integrations.http.FeignClientIntegration;
import com.axelixlabs.axelix.common.api.integrations.http.HttpVersion;
import com.axelixlabs.axelix.sbs.spring.core.integration.IntegrationComponentDiscoverer;

/**
 * Discovers HTTP service integrations based on {@link FeignClient} annotations
 * in the Spring application context.
 *
 * <p>This component scans all beans annotated with {@link FeignClient} and collects
 * metadata about the services they connect to, transforming them into
 * {@link FeignClientIntegration} objects.</p>
 *
 * @author Sergey Cherkasov
 */
public final class FeignClientIntegrationDiscoverer implements IntegrationComponentDiscoverer<FeignClientIntegration> {

    private final ApplicationContext context;
    private final DiscoveryClient discoveryClient;

    public FeignClientIntegrationDiscoverer(ApplicationContext context, DiscoveryClient discoveryClient) {
        this.context = context;
        this.discoveryClient = discoveryClient;
    }

    public Set<FeignClientIntegration> discoverIntegrations() {
        return Arrays.stream(context.getBeanNamesForAnnotation(FeignClient.class))
                .map(this::extractFeignClientClass)
                .filter(Objects::nonNull)
                .map(this::createIntegration)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private @Nullable FeignClientIntegration createIntegration(Class<?> feignType) {

        FeignClient feignClient = AnnotatedElementUtils.findMergedAnnotation(feignType, FeignClient.class);

        if (feignClient == null) {
            return null;
        }

        String serviceId = extractServiceId(feignClient);

        List<String> networkAddresses = extractNetworkAddresses(feignClient, serviceId);

        List<FeignClientIntegration.HttpMethod> methodMappings = Arrays.stream(feignType.getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .map(this::createHttpMethod)
                .filter(Objects::nonNull)
                .toList();

        return new FeignClientIntegration(networkAddresses, HttpVersion.V1_1.getDisplay(), serviceId, methodMappings);
    }

    private FeignClientIntegration.@Nullable HttpMethod createHttpMethod(Method method) {

        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        if (mapping == null) {
            return null;
        }

        String path = pickPath(mapping.path(), mapping.value());

        String httpMethod = mapping.method().length == 0 ? "UNKNOWN" : mapping.method()[0].name();

        return new FeignClientIntegration.HttpMethod(httpMethod, path);
    }

    private String pickPath(String[] path, String[] value) {
        if (path.length > 0) {
            return path[0];
        }
        if (value.length > 0) {
            return value[0];
        }

        return "";
    }

    private @Nullable Class<?> extractFeignClientClass(String beanName) {

        Object bean = context.getBean(beanName);
        Class<?> beanClass = bean.getClass();

        if (AnnotatedElementUtils.hasAnnotation(beanClass, FeignClient.class)) {
            return beanClass;
        }

        for (Class<?> iface : beanClass.getInterfaces()) {
            if (AnnotatedElementUtils.hasAnnotation(iface, FeignClient.class)) {
                return iface;
            }
        }

        return null;
    }

    private String extractServiceId(FeignClient feignClient) {
        if (!feignClient.name().isBlank()) {
            return feignClient.name();
        }
        if (!feignClient.value().isBlank()) {
            return feignClient.value();
        }
        return "unknown";
    }

    private List<String> extractNetworkAddresses(FeignClient feignClient, String serviceId) {

        if (!feignClient.url().isBlank()) {
            return List.of(feignClient.url());
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        List<String> discovered = instances.stream()
                .map(i -> i.getUri().toString())
                .filter(s -> !s.isBlank())
                .toList();

        return discovered.isEmpty() ? List.of("discovered://" + serviceId) : discovered;
    }
}
