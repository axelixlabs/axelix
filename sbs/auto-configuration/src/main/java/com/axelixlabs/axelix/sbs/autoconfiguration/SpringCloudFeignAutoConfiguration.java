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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import feign.Feign;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.axelixlabs.axelix.sbs.spring.integrations.http.FeignClientIntegrationDiscoverer;
import com.axelixlabs.axelix.sbs.spring.integrations.http.HttpIntegration;

/**
 * Auto-configuration for discovering HTTP integrations based on Spring Cloud OpenFeign.
 * <p>
 * Registers a {@link FeignClientIntegrationDiscoverer} if Feign is present on the classpath.
 * </p>
 *
 * @author Nikita Kirillov
 * @since 09.07.2025
 */
@AutoConfiguration
@ConditionalOnClass({Feign.class, FeignClient.class})
public class SpringCloudFeignAutoConfiguration {

    @Bean
    public IntegrationComponentDiscoverer<HttpIntegration> feignClientIntegrationDiscoverer(
            ApplicationContext context) {
        return new FeignClientIntegrationDiscoverer(context);
    }
}
