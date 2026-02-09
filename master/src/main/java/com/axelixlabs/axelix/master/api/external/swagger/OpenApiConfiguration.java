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
package com.axelixlabs.axelix.master.api.external.swagger;

import org.springdoc.core.customizers.OperationCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom components extending OpenAPI annotations.
 *
 * @author Sergey Cherkasov
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Defines the handling logic for {@link DefaultApiResponse} custom annotation.
     */
    @Bean
    public OperationCustomizer apiDefaultApiResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            DefaultApiResponse apiResponse = handlerMethod.getMethodAnnotation(DefaultApiResponse.class);

            if (apiResponse == null) {
                return operation;
            } else {
                operation.setSummary(apiResponse.summary());
                operation.setDescription(apiResponse.description());
            }

            return operation;
        };
    }
}
