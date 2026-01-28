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
package com.axelixlabs.axelix.sbs.spring.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.axelixlabs.axelix.common.api.registration.ServiceMetadata;

/**
 * Custom Spring Boot Actuator endpoint. Provides access to basic build information
 * such as the application version.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axelix-metadata")
public class AxelixMetadataEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AxelixMetadataEndpoint.class);

    private final ServiceMetadataAssembler serviceMetadataAssembler;

    public AxelixMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        this.serviceMetadataAssembler = serviceMetadataAssembler;
    }

    @ReadOperation
    public ServiceMetadata getMetadata() {
        log.debug("Received request for metadata assembly");

        ServiceMetadata serviceMetadata = serviceMetadataAssembler.assemble();

        log.debug("Assembled metadata for this service : {}", serviceMetadata);

        return serviceMetadata;
    }
}
