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
package com.axelixlabs.axelix.master.api.external.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.dependencies.DependencyAnalysisResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;

/**
 * The API for analyzing the runtime dependencies of managed instances.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class DependenciesApi {

    private final DependencyAnalysisService dependencyAnalysisService;

    public DependenciesApi(DependencyAnalysisService dependencyAnalysisService) {
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    /**
     * Retrieve the dependency analysis of the given instance: the framework maintenance window and every dependency
     * resolved onto the runtime classpath, joined with the curated catalog.
     */
    @GetMapping(path = ApiPaths.DependenciesApi.INSTANCE_ID)
    public DependencyAnalysisResponse getDependencyAnalysis(@PathVariable("instanceId") String instanceId) {
        return dependencyAnalysisService.analyzeFor(InstanceId.of(instanceId));
    }
}
