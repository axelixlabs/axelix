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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthorizationFilter;
import com.axelixlabs.axelix.sbs.spring.core.auth.WebIdentityAccessManager;

/**
 * Registers {@link JwtAuthorizationFilter} in the management child context, i.e. when Actuator
 * runs on a separate {@code management.server.port}.
 *
 * @since 19.10.2026
 * @author Nikita Kirillov
 */
@ManagementContextConfiguration(value = ManagementContextType.CHILD, proxyBeanMethods = false)
@ConditionalOnBean(WebIdentityAccessManager.class)
public class JwtAuthManagementContextConfiguration {

    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegistration(
            WebIdentityAccessManager webIdentityAccessManager,
            SecurityContextExecutor securityContextExecutor,
            WebEndpointProperties webEndpointProperties) {

        var jwtAuthorizationFilter = new JwtAuthorizationFilter(
                webIdentityAccessManager, securityContextExecutor, webEndpointProperties.getBasePath());
        var registration = new FilterRegistrationBean<>(jwtAuthorizationFilter);
        registration.setName("jwtAuthorizationFilter");
        return registration;
    }
}
