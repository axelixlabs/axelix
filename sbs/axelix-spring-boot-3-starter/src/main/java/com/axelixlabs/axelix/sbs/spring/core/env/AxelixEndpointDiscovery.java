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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Discovers Axelix endpoint IDs from the starter's endpoint declarations.
 *
 * @author Marsel Semenov
 * @author Andrey Kotov
 */
public class AxelixEndpointDiscovery {

    /**
     * Finds endpoint IDs declared directly with {@link Endpoint @Endpoint} or through a meta-annotation,
     * including endpoints disabled or excluded from exposure in the application.
     *
     * @param environment the application's environment
     * @return the declared Axelix endpoint IDs in alphabetical order
     */
    public List<EndpointId> discoverEndpointIds(ConfigurableEnvironment environment) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Endpoint.class));
        return scanner.findCandidateComponents("com.axelixlabs.axelix.sbs").stream()
                .map(candidate -> ((AnnotatedBeanDefinition) candidate)
                        .getMetadata()
                        .getAnnotations()
                        .get(Endpoint.class)
                        .getString("id"))
                .filter(id -> id.startsWith("axelix-"))
                .sorted()
                .map(EndpointId::of)
                .collect(Collectors.toList());
    }
}
