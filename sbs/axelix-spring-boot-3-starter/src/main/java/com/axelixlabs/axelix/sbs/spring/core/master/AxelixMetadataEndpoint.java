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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadatav2.MetadataV2;

/**
 * Custom Spring Boot Actuator endpoint. Provides access to basic build information
 * such as the application version.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-metadata")
public class AxelixMetadataEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AxelixMetadataEndpoint.class);

    private final BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler;
    private final MetadataV2Assembler metadataV2Assembler;

    public AxelixMetadataEndpoint(
            BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler,
            MetadataV2Assembler metadataV2Assembler) {
        this.basicRegistrationMetadataAssembler = basicRegistrationMetadataAssembler;
        this.metadataV2Assembler = metadataV2Assembler;
    }

    @GetMapping
    public BasicRegistrationMetadata getMetadata() {
        log.debug("Received request for metadata assembly");

        BasicRegistrationMetadata serviceMetadata = basicRegistrationMetadataAssembler.assemble();

        log.debug("Assembled metadata for this service : {}", serviceMetadata);

        return serviceMetadata;
    }

    @GetMapping("/v2")
    public MetadataV2 getMetadataV2() {
        return metadataV2Assembler.assemble();
    }
}
