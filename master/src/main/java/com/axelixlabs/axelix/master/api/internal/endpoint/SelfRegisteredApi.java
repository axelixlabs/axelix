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
package com.axelixlabs.axelix.master.api.internal.endpoint;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.master.api.internal.ApiPaths;
import com.axelixlabs.axelix.master.api.internal.InternalApiRestController;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.InstanceFactory;
import com.axelixlabs.axelix.master.service.InstanceRegistrar;
import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The API used for service self-registration.
 *
 * @author Sergey Cherkasov
 */
@Hidden
@InternalApiRestController
@RequestMapping(path = ApiPaths.SelfRegistryApi.MAIN)
@ConditionalOnProperty(prefix = "axelix.master.discovery", name = "auto", havingValue = "false")
public class SelfRegisteredApi {

    private final InstanceRegistrar instanceRegistrar;
    private final InstanceFactory instanceFactory;

    public SelfRegisteredApi(InstanceRegistrar instanceRegistrar, InstanceFactory instanceFactory) {
        this.instanceRegistrar = instanceRegistrar;
        this.instanceFactory = instanceFactory;
    }

    @PostMapping(path = ApiPaths.SelfRegistryApi.SERVICE_REGISTER)
    public ResponseEntity<Void> registryServiceInstance(@RequestBody SelfRegistrationMetadata request) {

        Instance instance = instanceFactory.createInstance(
                request.getInstanceId(),
                request.getInstanceName(),
                request.getDeploymentAt(),
                request.getInstanceUrl(),
                request.getBasicDiscoveryMetadata());
        instanceRegistrar.register(instance);

        return ResponseEntity.noContent().build();
    }
}
