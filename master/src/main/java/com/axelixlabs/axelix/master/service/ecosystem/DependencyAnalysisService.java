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
package com.axelixlabs.axelix.master.service.ecosystem;

import com.axelixlabs.axelix.master.api.external.response.dependencies.DependencyAnalysisResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.exception.SbomNotAvailableException;

/**
 * Analyzes the runtime dependencies of a managed instance.
 *
 * @author Mikhail Polivakha
 */
public interface DependencyAnalysisService {

    /**
     * @param instanceId the instance to analyze
     *
     * @return the dependency analysis of the instance
     *
     * @throws InstanceNotFoundException when the instance is not known to this Axelix Master
     * @throws SbomNotAvailableException when the instance serves no SBOM, i.e. the application was built without an
     *                                   Axelix build plugin
     */
    DependencyAnalysisResponse analyzeFor(InstanceId instanceId);
}
