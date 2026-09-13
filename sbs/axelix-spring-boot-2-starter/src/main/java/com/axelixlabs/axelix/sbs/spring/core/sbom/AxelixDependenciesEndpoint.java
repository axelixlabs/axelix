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
package com.axelixlabs.axelix.sbs.spring.core.sbom;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Custom actuator endpoint that serves the CycloneDX dependency SBOM the Axelix build plugins
 * package into the application archive.
 *
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-dependencies")
public class AxelixDependenciesEndpoint {

    private final ClasspathDependencySbom sbom;

    public AxelixDependenciesEndpoint(ClasspathDependencySbom sbom) {
        this.sbom = sbom;
    }

    @GetMapping
    public ResponseEntity<byte[]> sbom() {
        return sbom.read()
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
