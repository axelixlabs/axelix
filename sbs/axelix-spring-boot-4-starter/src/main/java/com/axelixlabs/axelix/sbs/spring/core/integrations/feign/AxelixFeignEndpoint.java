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
package com.axelixlabs.axelix.sbs.spring.core.integrations.feign;

import java.util.Set;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.axelixlabs.axelix.common.api.integration.FeignIntegration;

/**
 * Custom endpoint to expose Feign Client information.
 *
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axelix-feign")
public class AxelixFeignEndpoint {

    private final FeignClientIntegrationDiscoverer discoverer;

    public AxelixFeignEndpoint(FeignClientIntegrationDiscoverer discoverer) {
        this.discoverer = discoverer;
    }

    @ReadOperation
    public Set<FeignIntegration> feignClient() {
        return discoverer.discoverIntegrations();
    }
}
