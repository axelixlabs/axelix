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
package com.axelixlabs.axelix.sbs.spring.integrations.http;

import java.util.HashSet;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;

import com.axelixlabs.axelix.sbs.spring.integrations.IntegrationComponentDiscoverer;

/**
 * Discovers HTTP service integrations based on {@link FeignClient} annotations
 * in the Spring application context.
 *
 * <p>This component scans all beans annotated with {@link FeignClient} and collects
 * metadata about the services they connect to, transforming them into
 * {@link HttpIntegration} objects.</p>
 *
 * @since 09.07.2025
 * @author Nikita Kirillov
 */
public class FeignClientIntegrationDiscoverer implements IntegrationComponentDiscoverer<HttpIntegration> {

    private final ApplicationContext context;

    public FeignClientIntegrationDiscoverer(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Set<HttpIntegration> discoverIntegrations() {
        Set<HttpIntegration> integrations = new HashSet<>();

        for (String beanName : context.getBeanNamesForAnnotation(FeignClient.class)) {
            FeignClient feignClient = extractFeignClientAnnotation(beanName);
            if (feignClient != null) {
                integrations.add(createIntegration(feignClient));
            }
        }

        return integrations;
    }

    @SuppressWarnings("NullAway") // Suppress NullAway because the FeignClient annotation cannot be null here.
    private FeignClient extractFeignClientAnnotation(String beanName) {
        Object bean = context.getBean(beanName);
        Class<?> beanClass = bean.getClass();

        FeignClient annotation = beanClass.getAnnotation(FeignClient.class);
        if (annotation != null) {
            return annotation;
        }

        for (Class<?> aClass : beanClass.getInterfaces()) {
            annotation = aClass.getAnnotation(FeignClient.class);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    private HttpIntegration createIntegration(FeignClient annotation) {
        String entityType = annotation.name().isBlank() ? "Unknown" : annotation.name();
        String address = annotation.url().isBlank() ? "discovered://" + entityType : annotation.url();

        return new HttpIntegration(address, HttpVersion.V1_1, entityType);
    }
}
