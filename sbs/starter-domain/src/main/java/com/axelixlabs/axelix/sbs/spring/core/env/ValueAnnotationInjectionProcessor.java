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
import java.util.Map;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionPoint;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.InjectionType;

/**
 * Processes injection expressions originating from {@code @Value}-based bindings.
 *
 * <p>This abstraction extracts referenced property names from the provided expression
 * and associates them with {@link InjectionPoint injection points}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public interface ValueAnnotationInjectionProcessor {

    /**
     * Processes a single value injection expression and records all resolved property
     * references in the provided mapping.
     *
     * @param propertyToInjectionPoints  mapping between normalized property names and their injection points
     * @param expression                 the raw expression taken from the injection point
     * @param beanName                   the name of the bean where the injection happens
     * @param injectionType              the kind of injection point being processed
     * @param targetName                 the field or method parameter name that receives the injected value
     */
    void processValueAnnotation(
            Map<String, List<InjectionPoint>> propertyToInjectionPoints,
            String expression,
            String beanName,
            InjectionType injectionType,
            String targetName);
}
