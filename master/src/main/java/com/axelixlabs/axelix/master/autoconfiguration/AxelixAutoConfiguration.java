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

import java.net.http.HttpClient;
import java.time.Duration;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.version.CachingAxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.version.PropertiesAxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;
import com.axelixlabs.axelix.master.api.error.handle.ApiExceptionTranslator;
import com.axelixlabs.axelix.master.filter.ExceptionHandlingFilter;
import com.axelixlabs.axelix.master.service.auth.CookieService;

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
        return new CachingAxelixVersionDiscoverer(new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties"));
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public ExceptionHandlingFilter exceptionHandlingFilter(
            ApiExceptionTranslator apiExceptionTranslator, JsonMapper jsonMapper, CookieService cookieService) {
        return new ExceptionHandlingFilter(apiExceptionTranslator, jsonMapper, cookieService);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    /**
     * The {@link HttpClient} shared among all of the endpoint probers. Each {@link HttpClient} owns its
     * own selector thread and executor, so it is intentionally shared rather than created per prober.
     */
    @Bean
    public HttpClient endpointProbersHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }
}
