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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.security.Principal;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.AxelixPrincipal;
import com.axelixlabs.axelix.sbs.spring.core.contract.configprops.ConfigurationPropertiesFeed;

/**
 * Custom Actuator endpoint exposing the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-configprops")
public class AxelixConfigurationPropertiesEndpoint {

    private final ConfigurationPropertiesService configurationPropertiesService;
    private final SecurityContextExecutor securityContextExecutor;

    public AxelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesService configurationPropertiesService,
            SecurityContextExecutor securityContextExecutor) {
        this.configurationPropertiesService = configurationPropertiesService;
        this.securityContextExecutor = securityContextExecutor;
    }

    @GetMapping
    public ConfigurationPropertiesFeed configurationProperties(@Nullable Principal principal) {
        if (principal instanceof AxelixPrincipal axelixPrincipal) {
            // we're using reactive
            return securityContextExecutor.callWithinSecurityContext(
                    configurationPropertiesService::getConfigProps, axelixPrincipal.securityContext());
        } else {
            // we're working on the standard servlet app, JwtAuthorizationFilter has already set the security context
            return configurationPropertiesService.getConfigProps();
        }
    }
}
