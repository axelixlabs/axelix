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
package com.axelixlabs.axelix.master.autoconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.PropertiesAxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.api.error.handle.ApiExceptionTranslator;
import com.axelixlabs.axelix.master.exception.ExceptionHandlingFilter;

/**
 * General Auto-configuration of Axelix project.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class AxelixAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties");
    }

    @Bean
    public ExceptionHandlingFilter exceptionHandlingFilter(
            ApiExceptionTranslator apiExceptionTranslator, ObjectMapper objectMapper) {
        return new ExceptionHandlingFilter(apiExceptionTranslator, objectMapper);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
